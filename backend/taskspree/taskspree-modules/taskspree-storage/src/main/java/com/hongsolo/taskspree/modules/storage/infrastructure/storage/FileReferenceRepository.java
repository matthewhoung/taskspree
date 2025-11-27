package com.hongsolo.taskspree.modules.storage.infrastructure.storage;

import com.hongsolo.taskspree.modules.storage.domain.FileReference;
import com.hongsolo.taskspree.modules.storage.domain.repository.IFileReferenceRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileReferenceRepository
        extends JpaRepository<FileReference, UUID>, IFileReferenceRepository {

    List<FileReference> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    @Query("""
            SELECT fr FROM FileReference fr
            WHERE fr.file.id = :fileId
            AND fr.entityType = :entityType
            AND fr.entityId = :entityId
            """)
    Optional<FileReference> findByFileIdAndEntityTypeAndEntityId(
            @Param("fileId") UUID fileId,
            @Param("entityType") String entityType,
            @Param("entityId") UUID entityId
    );

    @Query("SELECT COUNT(fr) > 0 FROM FileReference fr WHERE fr.file.id = :fileId")
    boolean existsByFileId(@Param("fileId") UUID fileId);

    @Query("""
            SELECT COUNT(fr) > 0 FROM FileReference fr
            WHERE fr.file.id = :fileId
            AND fr.entityType = :entityType
            AND fr.entityId = :entityId
            """)
    boolean existsByFileIdAndEntityTypeAndEntityId(
            @Param("fileId") UUID fileId,
            @Param("entityType") String entityType,
            @Param("entityId") UUID entityId
    );

    @Modifying
    @Query("""
            DELETE FROM FileReference fr
            WHERE fr.entityType = :entityType
            AND fr.entityId = :entityId
            """)
    void deleteByEntityTypeAndEntityId(
            @Param("entityType") String entityType,
            @Param("entityId") UUID entityId
    );
}
