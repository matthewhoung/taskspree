package com.hongsolo.taskspree.modules.storage.domain.repository;

import com.hongsolo.taskspree.modules.storage.domain.enums.FileStatus;
import com.hongsolo.taskspree.modules.storage.domain.StoredFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IStoredFileRepository {

    StoredFile save(StoredFile storedFile);

    Optional<StoredFile> findById(UUID id);

    List<StoredFile> findAllById(Iterable<UUID> ids);

    Optional<StoredFile> findByIdAndUploaderId(UUID id, UUID uploaderId);

    List<StoredFile> findByUploaderId(UUID uploaderId);

    /**
     * Find files pending upload or failed (for retry)
     */
    List<StoredFile> findPendingUploads(int maxRetries, int limit);

    /**
     * Find orphaned files (pending for too long without being linked)
     */
    List<StoredFile> findOrphanedFiles(Instant createdBefore, int limit);

    /**
     * Count files by status for a user
     */
    long countByUploaderIdAndStatus(UUID uploaderId, FileStatus status);

    /**
     * Check if user has reached concurrent upload limit
     */
    long countPendingByUploaderId(UUID uploaderId);
}
