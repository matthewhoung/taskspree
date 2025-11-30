package com.hongsolo.taskspree.common.application.services;

import com.hongsolo.taskspree.common.domain.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * File storage service abstraction.
 * Implemented by taskspree-storage module, used by other modules.
 */
public interface IFileStorageService {

    /**
     * Initiates async upload for multiple files.
     *
     * @param files  List of multipart files to upload
     * @param userId ID of the user uploading the files
     * @return List of upload results with file IDs and initial status
     */
    List<FileUploadResult> initiateUpload(List<MultipartFile> files, UUID userId);

    /**
     * Get status of uploaded files
     *
     * @param fileIds List of file IDs to check
     * @return List of file statuses
     */
    List<FileStatusDto> getFileStatuses(List<UUID> fileIds);

    /**
     * Get presigned download URL (1 hour expiry by default)
     *
     * @param fileId ID of the file
     * @return Result with presigned URL or error
     */
    Result<String> getDownloadUrl(UUID fileId);

    /**
     * Link uploaded file to an entity (e.g., task, comment)
     *
     * @param fileId     ID of the file
     * @param entityType Type of entity (e.g., "TASK", "TASK_COMMENT")
     * @param entityId   ID of the entity
     */
    void linkToEntity(UUID fileId, String entityType, UUID entityId);

    /**
     * Unlink and soft-delete file
     *
     * @param fileId ID of the file to delete
     * @param userId ID of the user requesting deletion
     * @return Result indicating success or error
     */
    Result<Void> deleteFile(UUID fileId, UUID userId);

    /**
     * Result of initiating a file upload
     */
    record FileUploadResult(
            UUID fileId,
            String originalName,
            long fileSize,
            String status,
            String errorMessage
    ) {
        public static FileUploadResult success(UUID fileId, String originalName, long fileSize) {
            return new FileUploadResult(fileId, originalName, fileSize, "PENDING", null);
        }

        public static FileUploadResult failure(String originalName, long fileSize, String errorMessage) {
            return new FileUploadResult(null, originalName, fileSize, "REJECTED", errorMessage);
        }
    }

    /**
     * File status DTO
     */
    record FileStatusDto(
            UUID fileId,
            String originalName,
            long fileSize,
            String contentType,
            String status,
            String errorMessage,
            String downloadUrl
    ) {
    }
}