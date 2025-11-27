package com.hongsolo.taskspree.modules.storage.infrastructure.storage;

import com.hongsolo.taskspree.modules.storage.domain.enums.FileStatus;
import com.hongsolo.taskspree.modules.storage.domain.StoredFile;
import com.hongsolo.taskspree.modules.storage.domain.repository.IStoredFileRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoredFileRepository
        extends JpaRepository<StoredFile, UUID>, IStoredFileRepository {

    Optional<StoredFile> findByIdAndUploaderId(UUID id, UUID uploaderId);

    List<StoredFile> findByUploaderId(UUID uploaderId);

    long countByUploaderIdAndStatus(UUID uploaderId, FileStatus status);

    /**
     * Find files pending upload (PENDING or FAILED with retries remaining)
     */
    @Query("""
            SELECT f FROM StoredFile f
            WHERE f.status IN ('PENDING', 'FAILED')
            AND f.retryCount < :maxRetries
            AND f.deletedAt IS NULL
            ORDER BY f.createdAt ASC
            """)
    List<StoredFile> findPendingUploadsQuery(
            @Param("maxRetries") int maxRetries,
            org.springframework.data.domain.Pageable pageable
    );

    /**
     * Find orphaned files (pending for too long)
     */
    @Query("""
            SELECT f FROM StoredFile f
            WHERE f.status = 'PENDING'
            AND f.createdAt < :createdBefore
            AND f.deletedAt IS NULL
            ORDER BY f.createdAt ASC
            """)
    List<StoredFile> findOrphanedFilesQuery(
            @Param("createdBefore") Instant createdBefore,
            org.springframework.data.domain.Pageable pageable
    );

    /**
     * Count pending uploads for a user (for rate limiting)
     */
    @Query("""
            SELECT COUNT(f) FROM StoredFile f
            WHERE f.uploaderId = :uploaderId
            AND f.status IN ('PENDING', 'UPLOADING')
            AND f.deletedAt IS NULL
            """)
    long countPendingByUploaderIdQuery(@Param("uploaderId") UUID uploaderId);

    // ==================== Default Method Bridges ====================

    @Override
    default List<StoredFile> findPendingUploads(int maxRetries, int limit) {
        return findPendingUploadsQuery(maxRetries,
                org.springframework.data.domain.PageRequest.of(0, limit));
    }

    @Override
    default List<StoredFile> findOrphanedFiles(Instant createdBefore, int limit) {
        return findOrphanedFilesQuery(createdBefore,
                org.springframework.data.domain.PageRequest.of(0, limit));
    }

    @Override
    default long countPendingByUploaderId(UUID uploaderId) {
        return countPendingByUploaderIdQuery(uploaderId);
    }
}
