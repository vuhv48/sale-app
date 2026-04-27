CREATE TABLE batch_failed_records (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_name       VARCHAR(100) NOT NULL,
    execution_id   BIGINT,
    step_name      VARCHAR(100),
    phase          VARCHAR(20) NOT NULL,
    record_key     VARCHAR(255),
    error_type     VARCHAR(255),
    error_message  TEXT NOT NULL,
    payload        JSONB,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_batch_failed_records_job_exec
    ON batch_failed_records(job_name, execution_id, created_at DESC);

CREATE INDEX idx_batch_failed_records_phase
    ON batch_failed_records(phase, created_at DESC);
