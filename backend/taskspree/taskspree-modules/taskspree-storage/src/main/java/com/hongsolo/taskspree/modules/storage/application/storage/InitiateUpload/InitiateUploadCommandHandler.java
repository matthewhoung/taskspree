package com.hongsolo.taskspree.modules.storage.application.storage.InitiateUpload;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.application.services.IFileStorageService.FileUploadResult;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.storage.application.services.FileValidationService;
import com.hongsolo.taskspree.modules.storage.domain.StorageErrors;
import com.hongsolo.taskspree.modules.storage.domain.StoredFile;
import com.hongsolo.taskspree.modules.storage.domain.repository.IStoredFileRepository;
import com.hongsolo.taskspree.modules.storage.infrastructure.s3.S3Properties;
import com.hongsolo.taskspree.modules.storage.infrastructure.s3.S3StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitiateUploadCommandHandler
        implements CommandHandler<InitiateUploadCommand, Result<List<FileUploadResult>>> {

    private final IStoredFileRepository storedFileRepository;
    private final FileValidationService validationService;
    private final S3StorageClient s3StorageClient;
    private final S3Properties s3Properties;

    @Override
    public Result<List<FileUploadResult>> handle(InitiateUploadCommand command) {
        log.info("Processing upload for {} files from user {}",
                command.files().size(), command.uploaderId());

        // 1. GLOBAL VALIDATIONS
        // These are fast, in-memory checks.
        Optional<String> countError = validationService.validateFileCount(command.files().size());
        if (countError.isPresent()) {
            return Result.failure(StorageErrors.NO_FILES_PROVIDED);
        }

        // 2. RATE LIMIT CHECK (DB HIT)
        // We do this once before spawning threads to save resources.
        // Repository calls are implicitly Transactional (ReadOnly), so this is safe.
        long pendingCount = storedFileRepository.countPendingByUploaderId(command.uploaderId());
        if (validationService.hasReachedUploadLimit(pendingCount)) {
            return Result.failure(StorageErrors.TOO_MANY_FILES);
        }

        String bucketName = String.valueOf(s3Properties.getBuckets());

        // 3. PARALLEL EXECUTION (Virtual Threads)
        // We use try-with-resources to automatically close the scope.
        // We specify <FileUploadResult> to enforce strict typing.
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            // 1. Prepare the tasks
            List<Callable<FileUploadResult>> tasks = new ArrayList<>();
            for (MultipartFile file : command.files()) {
                tasks.add(() -> processSingleFile(file, command.uploaderId(), bucketName));
            }

            // 2. Run all efficiently (The 'try' block waits for them to finish)
            List<Future<FileUploadResult>> futures = executor.invokeAll(tasks);

            // 3. Unwrap results
            List<FileUploadResult> results = futures.stream()
                    .map(f -> {
                        try {
                            return f.get();
                        } catch (Exception e) {
                            return FileUploadResult.failure("unknown", 0, e.getMessage());
                        }
                    })
                    .toList();

            return Result.success(results);

        } catch (InterruptedException e) {
            log.error("Upload interrupted", e);
            Thread.currentThread().interrupt();
            return Result.failure(StorageErrors.UPLOAD_FAILED);
        }
    }

    /**
     * WORKER METHOD
     * This runs on its own Virtual Thread.
     * It manages its own granular transactions.
     */
    private FileUploadResult processSingleFile(MultipartFile file, UUID uploaderId, String bucketName) {
        String originalName = file.getOriginalFilename();
        long fileSize = file.getSize();
        String contentType = file.getContentType();

        try {
            // STEP A: Validate File (Memory)
            Optional<String> validationError = validationService.validateFile(file);
            if (validationError.isPresent()) {
                return FileUploadResult.failure(originalName, fileSize, validationError.get());
            }

            // STEP B: INITIAL DB INSERT (Transactional)
            // storedFileRepository.save() opens a transaction, inserts, commits, and closes.
            // This is very fast (milliseconds).
            StoredFile storedFile = StoredFile.create(
                    uploaderId,
                    originalName,
                    fileSize,
                    contentType,
                    null
            );

            // This 'save' gives us the ID needed for the S3 Key
            storedFile = storedFileRepository.save(storedFile);

            // Generate the key after we have the ID
            String s3Key = storedFile.generateStoredName();


            // STEP C: S3 UPLOAD (Long Running - NO TRANSACTION)
            // The DB connection is closed here. We are only using a Virtual Thread and Network Socket.
            s3StorageClient.uploadStream(
                    bucketName,
                    s3Key,
                    file.getInputStream(),
                    fileSize,
                    contentType
            );


            // STEP D: FINAL DB UPDATE (Transactional)
            // Re-open a short transaction to mark completion.
            storedFile.markCompleted(bucketName, s3Key);
            storedFileRepository.save(storedFile);

            return FileUploadResult.success(storedFile.getId(), originalName, fileSize);

        } catch (Exception e) {
            log.error("Failed to upload file {}: {}", originalName, e.getMessage());

            // OPTIONAL: If Step B succeeded but Step C failed, we technically have a PENDING orphan.
            // You could add a try-catch block around Step C to delete the DB record if upload fails.
            // However, the "Orphan Cleanup Job" you designed earlier covers this case elegantly.

            return FileUploadResult.failure(originalName, fileSize, "System error: " + e.getMessage());
        }
    }
}