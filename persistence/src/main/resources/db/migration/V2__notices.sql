-- =============================================================================
-- FILE: V2__notices.sql
-- PURPOSE (EN):
-- - Create the notices table for internal event/notification persistence.
-- - Support internal tracking and audit use cases without external delivery.
-- MUC DICH:
-- - Tao bang notices de luu thong bao/su kien noi bo dang log trong CSDL.
-- - Phuc vu nghiep vu theo doi/audit noi bo, chua dong bo ra he thong ngoai.
-- =============================================================================

CREATE TABLE notices (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notice_type  VARCHAR(128) NOT NULL,
    payload      TEXT         NOT NULL,
    is_deleted   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(255),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_by   VARCHAR(255)
);

CREATE INDEX idx_notices_notice_type ON notices (notice_type);
CREATE INDEX idx_notices_created_at ON notices (created_at DESC);

COMMENT ON TABLE notices IS 'Log thong bao noi bo; payload thuong la JSON string';
