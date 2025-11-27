-- 1. Create the Schema
CREATE SCHEMA IF NOT EXISTS storage;

-- 2. Create Stored Files Table
CREATE TABLE storage.stored_files (
    id UUID NOT NULL,
    uploader_id UUID NOT NULL,

    -- Original file info
    original_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,

    -- Temp storage (before S3 upload)
    temp_path VARCHAR(500),

    -- S3 storage (after successful upload)
    bucket_name VARCHAR(100),
    s3_key VARCHAR(500),

    -- Status tracking (outbox-style)
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,

    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    uploaded_at TIMESTAMP WITH TIME ZONE,

    -- Soft delete
    deleted_at TIMESTAMP WITH TIME ZONE,

    PRIMARY KEY (id)
);

-- 3. Create File References Table (links files to entities)
CREATE TABLE storage.file_references (
    id UUID NOT NULL,
    file_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_file_references_file_id
        FOREIGN KEY (file_id) REFERENCES storage.stored_files(id) ON DELETE CASCADE,
    CONSTRAINT uk_file_references_unique
        UNIQUE (file_id, entity_type, entity_id)
);

-- 4. Create Indexes
-- Index for async processor to find pending/failed uploads
CREATE INDEX idx_stored_files_pending
    ON storage.stored_files(status, retry_count)
    WHERE status IN ('PENDING', 'FAILED') AND retry_count < 3;

-- Index for orphan cleanup (files pending for too long without being linked)
CREATE INDEX idx_stored_files_orphan
    ON storage.stored_files(created_at, status)
    WHERE status = 'PENDING';

-- Index for querying files by uploader
CREATE INDEX idx_stored_files_uploader
    ON storage.stored_files(uploader_id);

-- Index for file references by entity
CREATE INDEX idx_file_references_entity
    ON storage.file_references(entity_type, entity_id);
