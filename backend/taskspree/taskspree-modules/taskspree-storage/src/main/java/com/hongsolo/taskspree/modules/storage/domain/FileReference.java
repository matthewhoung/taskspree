package com.hongsolo.taskspree.modules.storage.domain;

import com.hongsolo.taskspree.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_references", schema = "storage")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileReference extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private StoredFile file;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private FileReference(StoredFile file, String entityType, UUID entityId) {
        this.file = file;
        this.entityType = entityType;
        this.entityId = entityId;
        this.createdAt = Instant.now();
    }

    /**
     * Factory method to create a file reference
     */
    public static FileReference create(StoredFile file, String entityType, UUID entityId) {
        return new FileReference(file, entityType, entityId);
    }

    /**
     * Common entity types for reference
     */
    public static class EntityTypes {
        public static final String TASK = "TASK";
        public static final String TASK_COMMENT = "TASK_COMMENT";
        public static final String TASK_SUBMISSION = "TASK_SUBMISSION";
        public static final String USER_AVATAR = "USER_AVATAR";

        private EntityTypes() {
            // Prevent instantiation
        }
    }
}
