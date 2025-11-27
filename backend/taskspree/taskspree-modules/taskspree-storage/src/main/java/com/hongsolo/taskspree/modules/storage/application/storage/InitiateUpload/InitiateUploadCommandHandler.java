package com.hongsolo.taskspree.modules.storage.application.storage.InitiateUpload;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.application.services.IFileStorageService.FileUploadResult;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.storage.application.services.FileValidationService;
import com.hongsolo.taskspree.modules.storage.application.services.TempFileService;
import com.hongsolo.taskspree.modules.storage.domain.StorageErrors;
import com.hongsolo.taskspree.modules.storage.domain.StoredFile;
import com.hongsolo.taskspree.modules.storage.domain.repository.IStoredFileRepository;
import com.hongsolo.taskspree.modules.storage.infrastructure.s3.AsyncUploadProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitiateUploadCommandHandler
        implements CommandHandler<InitiateUploadCommand, Result<List<FileUploadResult>>> {

    private final IStoredFileRepository storedFileRepository;
    private final FileValidationService validationService;
    private final TempFileService tempFileService;
    private final AsyncUploadProcessor asyncUploadProcessor;

    @Override
    @Transactional
    public Result<List<FileUploadResult>> handle(InitiateUploadCommand command) {
        log.info("Processing upload initiation for {} files from user {}",
                command.files().size(), command.uploaderId());

        // Validate file count
        Optional<String> countError = validationService.validateFileCount(command.files().size());
        if (countError.isPresent()) {
            log.warn("File count validation failed: {}", countError.get());
            return Result.failure(StorageErrors.NO_FILES_PROVIDED);
        }

        // Check user's pending upload count
        long pendingCount = storedFileRepository.countPendingByUploaderId(command.uploaderId());
        if (validationService.hasReachedUploadLimit(pendingCount)) {
            log.warn("User {} has reached upload limit: {}", command.uploaderId(), pendingCount);
            return Result.failure(StorageErrors.TOO_MANY_FILES);
        }

        List<FileUploadResult> results = new ArrayList<>();

        for (MultipartFile file : command.files()) {
            FileUploadResult result = processFile(file, command.uploaderId());
            results.add(result);
        }

        log.info("Upload initiation complete: {} successful, {} failed",
                results.stream().filter(r -> r.fileId() != null).count(),
                results.stream().filter(r -> r.fileId() == null).count());

        return Result.success(results);
    }

    /**
     * Process a single file: validate, save to temp, create record, queue async upload
     */
    private FileUploadResult processFile(MultipartFile file, java.util.UUID uploaderId) {
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
        long fileSize = file.getSize();

        try {
            // Validate the file
            Optional<String> validationError = validationService.validateFile(file);
            if (validationError.isPresent()) {
                log.warn("File validation failed for {}: {}", originalName, validationError.get());
                return FileUploadResult.failure(originalName, fileSize, validationError.get());
            }

            // Create StoredFile entity first to get ID
            StoredFile storedFile = StoredFile.create(
                    uploaderId,
                    originalName,
                    fileSize,
                    file.getContentType(),
                    null // temp path set after save to temp
            );
            storedFile = storedFileRepository.save(storedFile);

            // Save to temp storage
            Path tempPath = tempFileService.saveToTemp(file, storedFile.getId());

            // Update with temp path
            storedFile.setTempPath(tempPath.toString());
            storedFile = storedFileRepository.save(storedFile);

            // Queue async upload
            asyncUploadProcessor.processUpload(storedFile.getId());

            log.debug("File queued for upload: id={}, name={}", storedFile.getId(), originalName);

            return FileUploadResult.success(storedFile.getId(), originalName, fileSize);

        } catch (Exception e) {
            log.error("Failed to process file {}: {}", originalName, e.getMessage(), e);
            return FileUploadResult.failure(originalName, fileSize, "Failed to save file: " + e.getMessage());
        }
    }
}
