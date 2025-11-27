package com.hongsolo.taskspree.modules.storage.domain;

import com.hongsolo.taskspree.common.domain.BaseEntity;
import com.hongsolo.taskspree.modules.storage.domain.enums.FileStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stored_files", schema = "storage")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoredFile extends BaseEntity {

    @Column(name = "uploader_id", nullable = false)
    private UUID uploaderId;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "temp_path", length = 500)
    private String tempPath;

    @Column(name = "bucket_name", length = 100)
    private String bucketName;

    @Column(name = "s3_key", length = 500)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FileStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    private StoredFile(
            UUID uploaderId,
            String originalName,
            Long fileSize,
            String contentType,
            String tempPath
    ) {
        this.uploaderId = uploaderId;
        this.originalName = originalName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.tempPath = tempPath;
        this.status = FileStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = Instant.now();
    }

    /**
     * Factory method to create a new StoredFile in PENDING status
     */
    public static StoredFile create(
            UUID uploaderId,
            String originalName,
            Long fileSize,
            String contentType,
            String tempPath
    ) {
        return new StoredFile(uploaderId, originalName, fileSize, contentType, tempPath);
    }

    /**
     * Mark file as uploading to S3
     */
    public void markUploading() {
        this.status = FileStatus.UPLOADING;
    }

    /**
     * Mark file as successfully uploaded to S3
     */
    public void markCompleted(String bucketName, String s3Key) {
        this.status = FileStatus.COMPLETED;
        this.bucketName = bucketName;
        this.s3Key = s3Key;
        this.uploadedAt = Instant.now();
        this.tempPath = null; // Clear temp path as file is now in S3
        this.errorMessage = null;
    }

    /**
     * Mark file as failed to upload
     */
    public void markFailed(String errorMessage) {
        this.status = FileStatus.FAILED;
        this.retryCount++;
        this.errorMessage = errorMessage;
    }

    /**
     * Soft delete the file
     */
    public void markDeleted() {
        this.deletedAt = Instant.now();
    }

    /**
     * Check if file can be retried
     */
    public boolean canRetry(int maxRetries) {
        return this.status.canRetry() && this.retryCount < maxRetries;
    }

    /**
     * Check if file is ready for download
     */
    public boolean isAvailable() {
        return this.status == FileStatus.COMPLETED && this.deletedAt == null;
    }

    /**
     * Check if file is soft deleted
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Set the temp path after initial creation
     * (Used when file is saved to temp storage)
     */
    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    /**
     * Get the file extension from original name
     */
    public String getExtension() {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        return originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Generate a stored name (UUID-based) for S3
     */
    public String generateStoredName() {
        String extension = getExtension();
        return extension.isEmpty()
                ? getId().toString()
                : getId().toString() + "." + extension;
    }
}
