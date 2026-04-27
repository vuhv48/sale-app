-- =============================================================================
-- FILE: V15__document_types_and_versions.sql
-- PURPOSE:
-- - Add document_types (master) and document_versions (history table).
-- - Keep compatibility with existing documents table created in V14.
-- - Follow company audit convention: is_deleted, created_at, updated_at, created_by, updated_by.
-- =============================================================================

CREATE TABLE document_types (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                 VARCHAR(100) NOT NULL UNIQUE,
    name                 VARCHAR(255) NOT NULL,
    active               BOOLEAN      NOT NULL DEFAULT TRUE,
    require_verification BOOLEAN      NOT NULL DEFAULT TRUE,
    max_size_mb          INTEGER,
    allowed_mime_pattern VARCHAR(255),

    is_deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by           VARCHAR(255),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by           VARCHAR(255)
);

CREATE TABLE document_versions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id         UUID          NOT NULL REFERENCES documents(id),
    version_no          INTEGER       NOT NULL,
    storage_provider    VARCHAR(20)   NOT NULL DEFAULT 'minio',
    bucket              VARCHAR(255)  NOT NULL,
    file_path           VARCHAR(1024) NOT NULL,
    object_version      VARCHAR(255),
    etag                VARCHAR(128),
    original_filename   VARCHAR(512),
    mime_type           VARCHAR(255),
    size_bytes          BIGINT        NOT NULL CHECK (size_bytes >= 0),
    upload_status       VARCHAR(30)   NOT NULL DEFAULT 'UPLOADED',
    uploaded_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),

    is_deleted          BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_by          VARCHAR(255),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_by          VARCHAR(255),

    CONSTRAINT uq_document_versions_doc_version UNIQUE (document_id, version_no)
);

CREATE UNIQUE INDEX uq_document_versions_storage_path
    ON document_versions(storage_provider, bucket, file_path);

CREATE INDEX idx_document_versions_document_id
    ON document_versions(document_id)
    WHERE is_deleted = FALSE;

ALTER TABLE documents
    ADD COLUMN document_type_id UUID,
    ADD COLUMN current_version_id UUID;

ALTER TABLE documents
    ADD CONSTRAINT fk_documents_document_type
        FOREIGN KEY (document_type_id) REFERENCES document_types(id);

ALTER TABLE documents
    ADD CONSTRAINT fk_documents_current_version
        FOREIGN KEY (current_version_id) REFERENCES document_versions(id);

INSERT INTO document_types (code, name, active, require_verification)
VALUES ('GENERIC_FILE', 'Generic File', TRUE, FALSE)
ON CONFLICT (code) DO NOTHING;

