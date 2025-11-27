package com.hongsolo.taskspree.modules.storage.domain.repository;

import com.hongsolo.taskspree.modules.storage.domain.FileReference;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IFileReferenceRepository {

    FileReference save(FileReference fileReference);

    Optional<FileReference> findById(UUID id);

    /**
     * Find all files linked to an entity
     */
    List<FileReference> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    /**
     * Find reference by file and entity
     */
    Optional<FileReference> findByFileIdAndEntityTypeAndEntityId(
            UUID fileId,
            String entityType,
            UUID entityId
    );

    /**
     * Check if a file is linked to any entity
     */
    boolean existsByFileId(UUID fileId);

    /**
     * Check if a specific link exists
     */
    boolean existsByFileIdAndEntityTypeAndEntityId(
            UUID fileId,
            String entityType,
            UUID entityId
    );

    /**
     * Delete reference
     */
    void delete(FileReference fileReference);

    /**
     * Delete all references for an entity
     */
    void deleteByEntityTypeAndEntityId(String entityType, UUID entityId);
}
