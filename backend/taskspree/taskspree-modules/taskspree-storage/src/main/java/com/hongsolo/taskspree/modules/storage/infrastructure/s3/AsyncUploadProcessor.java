package com.hongsolo.taskspree.modules.storage.infrastructure.s3;

import com.hongsolo.taskspree.modules.storage.domain.StoredFile;
import com.hongsolo.taskspree.modules.storage.domain.repository.IStoredFileRepository;
import com.hongsolo.taskspree.modules.storage.infrastructure.s3.S3Properties;
import com.hongsolo.taskspree.modules.storage.infrastructure.storage.StorageProperties;
import com.hongsolo.taskspree.modules.storage.infrastructure.s3.S3StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncUploadProcessor {

    private final IStoredFileRepository storedFileRepository;
    private final S3StorageClient s3StorageClient;
    private final S3Properties s3Properties;
    private final StorageProperties storageProperties;

    /**
     * Process file upload asynchronously with retry support.
     * This method runs in the uploadExecutor thread pool.
     */
    @Async("uploadExecutor")
    @Retryable(
            retryFor = {Exception.class},
            maxAttemptsExpression = "${storage.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${storage.retry.delay-ms:5000}",
                    multiplierExpression = "${storage.retry.multiplier:2.0}"
            )
    )
    @Transactional
    public void processUpload(UUID fileId) {
        log.info("Starting async upload for file: {}", fileId);

        StoredFile storedFile = storedFileRepository.findById(fileId)
                .orElse(null);

        if (storedFile == null) {
            log.warn("File not found for upload: {}", fileId);
            return;
        }

        if (storedFile.getStatus().isTerminal()) {
            log.debug("File already in terminal state: id={}, status={}", fileId, storedFile.getStatus());
            return;
        }

        try {
            // Mark as uploading
            storedFile.markUploading();
            storedFileRepository.save(storedFile);

            // Get temp file path
            Path tempPath = Path.of(storedFile.getTempPath());

            if (!Files.exists(tempPath)) {
                throw new IOException("Temp file does not exist: " + tempPath);
            }

            // Determine bucket and key
            String bucketName = s3Properties.getBuckets().getTasks();
            String s3Key = generateS3Key(storedFile);

            // Upload to S3
            boolean success = s3StorageClient.uploadFile(
                    bucketName,
                    s3Key,
                    tempPath,
                    storedFile.getContentType()
            );

            if (success) {
                // Mark as completed
                storedFile.markCompleted(bucketName, s3Key);
                storedFileRepository.save(storedFile);

                // Delete temp file
                deleteTempFile(tempPath);

                log.info("Upload completed successfully: fileId={}, s3Key={}", fileId, s3Key);

                // TODO: Send WebSocket notification to user
                // notificationService.notifyUploadComplete(storedFile);
            }

        } catch (Exception e) {
            log.error("Upload failed for file: {}, error: {}", fileId, e.getMessage());
            throw new RuntimeException("Upload failed", e);
        }
    }

    /**
     * Recovery method when all retries are exhausted
     */
    @Recover
    @Transactional
    public void recoverUpload(Exception e, UUID fileId) {
        log.error("All upload retries exhausted for file: {}", fileId);

        storedFileRepository.findById(fileId).ifPresent(storedFile -> {
            storedFile.markFailed("Upload failed after " + storageProperties.getRetry().getMaxAttempts() +
                    " attempts: " + e.getMessage());
            storedFileRepository.save(storedFile);

            // TODO: Send WebSocket notification to user about failure
            // notificationService.notifyUploadFailed(storedFile);
        });
    }

    /**
     * Generate S3 key for a file.
     * Format: {fileId}-{sanitizedOriginalName}
     */
    private String generateS3Key(StoredFile storedFile) {
        String sanitizedName = sanitizeFileName(storedFile.getOriginalName());
        return storedFile.getId().toString() + "-" + sanitizedName;
    }

    /**
     * Sanitize file name for S3 key
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unknown";
        }
        // Replace spaces and special characters
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase();
    }

    /**
     * Delete temp file after successful upload
     */
    private void deleteTempFile(Path tempPath) {
        try {
            Files.deleteIfExists(tempPath);
            log.debug("Temp file deleted: {}", tempPath);
        } catch (IOException e) {
            log.warn("Failed to delete temp file: {}, error: {}", tempPath, e.getMessage());
            // Not critical - orphan cleanup will handle it later
        }
    }
}
