-- Document schema proposal (company style: 5 audit fields on every table)
-- Tables:
--   1) document_types
--   2) documents (logical)
--   3) document_versions (physical versions)
-- Notes:
--   - customers keeps id_front_document_id / id_back_document_id -> documents.id
--   - all tables include: is_deleted, created_at, updated_at, created_by, updated_by

CREATE TABLE IF NOT EXISTS document_types (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code varchar(100) NOT NULL UNIQUE,
    name varchar(255) NOT NULL,
    active boolean NOT NULL DEFAULT TRUE,
    require_verification boolean NOT NULL DEFAULT TRUE,
    max_size_mb integer,
    allowed_mime_pattern varchar(255),

    -- company-standard 5 fields
    is_deleted boolean NOT NULL DEFAULT FALSE,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by varchar(255),
    updated_by varchar(255)
);

CREATE TABLE IF NOT EXISTS documents (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    document_type_id uuid NOT NULL REFERENCES document_types(id),
    owner_type varchar(50) NOT NULL, -- CUSTOMER | INVOICE | ORDER | ...
    owner_id uuid NOT NULL,
    current_version_id uuid NULL,
    status varchar(30) NOT NULL DEFAULT 'PENDING', -- PENDING | VERIFIED | REJECTED | ARCHIVED

    -- company-standard 5 fields
    is_deleted boolean NOT NULL DEFAULT FALSE,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by varchar(255),
    updated_by varchar(255)
);

CREATE INDEX IF NOT EXISTS idx_documents_owner
    ON documents(owner_type, owner_id)
    WHERE is_deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_documents_type
    ON documents(document_type_id)
    WHERE is_deleted = FALSE;

CREATE TABLE IF NOT EXISTS document_versions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id uuid NOT NULL REFERENCES documents(id),
    version_no integer NOT NULL,
    storage_provider varchar(20) NOT NULL DEFAULT 'minio', -- minio | s3
    bucket varchar(255) NOT NULL,
    object_key varchar(1024) NOT NULL,
    object_version varchar(255),
    etag varchar(128),
    original_filename varchar(512),
    mime_type varchar(255),
    size_bytes bigint NOT NULL CHECK (size_bytes >= 0),
    checksum_sha256 varchar(64),
    upload_status varchar(30) NOT NULL DEFAULT 'UPLOADED', -- UPLOADED | FAILED
    uploaded_at timestamptz NOT NULL DEFAULT now(),

    -- company-standard 5 fields
    is_deleted boolean NOT NULL DEFAULT FALSE,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by varchar(255),
    updated_by varchar(255),

    CONSTRAINT uq_document_versions_doc_version UNIQUE (document_id, version_no)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_document_versions_storage
    ON document_versions(storage_provider, bucket, object_key)
    WHERE is_deleted = FALSE;

ALTER TABLE documents
    DROP CONSTRAINT IF EXISTS fk_documents_current_version;

ALTER TABLE documents
    ADD CONSTRAINT fk_documents_current_version
    FOREIGN KEY (current_version_id) REFERENCES document_versions(id);

-- Optional: wire customers fast-access FK columns if your table already has these columns:
-- ALTER TABLE customers
--   ADD CONSTRAINT fk_customers_front_document FOREIGN KEY (id_front_document_id) REFERENCES documents(id);
-- ALTER TABLE customers
--   ADD CONSTRAINT fk_customers_back_document FOREIGN KEY (id_back_document_id) REFERENCES documents(id);

