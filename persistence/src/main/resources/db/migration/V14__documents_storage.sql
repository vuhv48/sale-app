-- =============================================================================
-- FILE: V14__documents_storage.sql
-- PURPOSE:
-- - Add documents table to store uploaded file metadata (path in object storage).
-- - Follow company audit convention: is_deleted, created_at, updated_at, created_by, updated_by.
-- =============================================================================

CREATE TABLE documents (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider           VARCHAR(20)   NOT NULL DEFAULT 'minio',
    bucket             VARCHAR(255)  NOT NULL,
    file_path          VARCHAR(1024) NOT NULL,
    original_filename  VARCHAR(512),
    mime_type          VARCHAR(255),
    size_bytes         BIGINT        NOT NULL CHECK (size_bytes >= 0),
    status             VARCHAR(30)   NOT NULL DEFAULT 'UPLOADED',

    is_deleted         BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_by         VARCHAR(255),
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_by         VARCHAR(255)
);

CREATE UNIQUE INDEX uq_documents_provider_bucket_path
    ON documents(provider, bucket, file_path);

CREATE INDEX idx_documents_status
    ON documents(status)
    WHERE is_deleted = FALSE;
