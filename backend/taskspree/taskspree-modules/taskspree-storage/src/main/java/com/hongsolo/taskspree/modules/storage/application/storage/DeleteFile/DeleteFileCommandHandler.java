package com.hongsolo.taskspree.modules.storage.application.storage.DeleteFile;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.storage.application.services.TempFileService;
import com.hongsolo.taskspree.modules.storage.domain.StorageErrors;
import com.hongsolo.taskspree.modules.storage.domain.StoredFile;
import com.hongsolo.taskspree.modules.storage.domain.repository.IStoredFileRepository;
import com.hongsolo.taskspree.modules.storage.infrastructure.s3.S3StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteFileCommandHandler
        implements CommandHandler<DeleteFileCommand, Result<Void>> {

    private final IStoredFileRepository storedFileRepository;
    private final TempFileService tempFileService;
    private final S3StorageClient s3StorageClient;

    @Override
    @Transactional
    public Result<Void> handle(DeleteFileCommand command) {
        log.info("Processing file deletion: fileId={}, requesterId={}",
                command.fileId(), command.requesterId());

        // Find the file
        StoredFile storedFile = storedFileRepository.findById(command.fileId())
                .orElse(null);

        if (storedFile == null) {
            log.warn("File not found: {}", command.fileId());
            return Result.failure(StorageErrors.FILE_NOT_FOUND);
        }

        // Check if already deleted
        if (storedFile.isDeleted()) {
            log.debug("File already deleted: {}", command.fileId());
            return Result.success(null);
        }

        // Check ownership
        if (!storedFile.getUploaderId().equals(command.requesterId())) {
            log.warn("User {} attempted to delete file {} owned by {}",
                    command.requesterId(), command.fileId(), storedFile.getUploaderId());
            return Result.failure(StorageErrors.NOT_FILE_OWNER);
        }

        // Delete from temp if exists
        if (storedFile.getTempPath() != null) {
            tempFileService.deleteTempFile(storedFile.getTempPath());
        }

        // Delete from S3 if uploaded
        if (storedFile.getBucketName() != null && storedFile.getS3Key() != null) {
            try {
                s3StorageClient.deleteFile(storedFile.getBucketName(), storedFile.getS3Key());
            } catch (Exception e) {
                log.warn("Failed to delete file from S3: bucket={}, key={}, error={}",
                        storedFile.getBucketName(), storedFile.getS3Key(), e.getMessage());
                // Continue with soft delete even if S3 delete fails
            }
        }

        // Soft delete the record
        storedFile.markDeleted();
        storedFileRepository.save(storedFile);

        log.info("File deleted successfully: {}", command.fileId());
        return Result.success(null);
    }
}
