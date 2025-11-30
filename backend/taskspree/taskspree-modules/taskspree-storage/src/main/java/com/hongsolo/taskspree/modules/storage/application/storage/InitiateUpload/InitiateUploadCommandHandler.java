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
                command.files().size(), command.userId());

        // 1. GLOBAL VALIDATIONS
        Optional<String> countError = validationService.validateFileCount(command.files().size());
        if (countError.isPresent()) {
            return Result.failure(StorageErrors.NO_FILES_PROVIDED);
        }

        // 2. RATE LIMIT CHECK
        long pendingCount = storedFileRepository.countPendingByUserId(command.userId());
        if (validationService.hasReachedUploadLimit(pendingCount)) {
            return Result.failure(StorageErrors.TOO_MANY_FILES);
        }

        String bucketName = s3Properties.getBuckets().getTasks();

        // 3. PARALLEL EXECUTION (Virtual Threads)
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            List<Callable<FileUploadResult>> tasks = new ArrayList<>();
            for (MultipartFile file : command.files()) {
                tasks.add(() -> processSingleFile(file, command.userId(), bucketName));
            }

            List<Future<FileUploadResult>> futures = executor.invokeAll(tasks);

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

    private FileUploadResult processSingleFile(MultipartFile file, UUID userId, String bucketName) {
        String originalName = file.getOriginalFilename();
        long fileSize = file.getSize();
        String contentType = file.getContentType();

        try {
            // Validate File
            Optional<String> validationError = validationService.validateFile(file);
            if (validationError.isPresent()) {
                return FileUploadResult.failure(originalName, fileSize, validationError.get());
            }

            // Initial DB Insert
            StoredFile storedFile = StoredFile.create(
                    userId,
                    originalName,
                    fileSize,
                    contentType,
                    null
            );

            storedFile = storedFileRepository.save(storedFile);
            String s3Key = storedFile.generateStoredName();

            // S3 Upload
            s3StorageClient.uploadStream(
                    bucketName,
                    s3Key,
                    file.getInputStream(),
                    fileSize,
                    contentType
            );

            // Final DB Update
            storedFile.markCompleted(bucketName, s3Key);
            storedFileRepository.save(storedFile);

            return FileUploadResult.success(storedFile.getId(), originalName, fileSize);

        } catch (Exception e) {
            log.error("Failed to upload file {}: {}", originalName, e.getMessage());
            return FileUploadResult.failure(originalName, fileSize, "System error: " + e.getMessage());
        }
    }
}