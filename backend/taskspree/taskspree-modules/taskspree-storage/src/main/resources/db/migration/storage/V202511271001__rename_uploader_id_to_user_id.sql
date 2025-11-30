-- Rename uploader_id to user_id in stored_files table
ALTER TABLE storage.stored_files
    RENAME COLUMN uploader_id TO user_id;

-- Update index name for consistency
DROP INDEX IF EXISTS storage.idx_stored_files_uploader;
CREATE INDEX idx_stored_files_user_id ON storage.stored_files(user_id);