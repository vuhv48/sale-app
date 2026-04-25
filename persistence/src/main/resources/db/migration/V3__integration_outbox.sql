-- =============================================================================
-- FILE: V3__integration_outbox.sql
-- PURPOSE (EN):
-- - Create transactional outbox table for integration events.
-- - Ensure business data and outbound events are stored atomically.
-- - Enable relay workers to safely publish pending events to Kafka.
-- MUC DICH:
-- - Tao bang transactional outbox cho integration event.
-- - Dam bao ghi du lieu nghiep vu va ghi event cung mot transaction.
-- - Cho phep relay process doc PENDING event va day ra Kafka an toan.
-- =============================================================================
CREATE TABLE integration_outbox (
    id           UUID PRIMARY KEY,
    topic        VARCHAR(255) NOT NULL,
    message_key  VARCHAR(255) NOT NULL,
    payload      TEXT         NOT NULL,
    status       VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    sent_at      TIMESTAMPTZ,
    last_error   VARCHAR(2000)
);

CREATE INDEX idx_integration_outbox_pending ON integration_outbox (status, created_at)
    WHERE status = 'PENDING';

COMMENT ON TABLE integration_outbox IS 'Transactional outbox: PENDING -> relay Kafka -> SENT';
