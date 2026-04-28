ALTER TABLE batch_failed_records
    ADD COLUMN retry_count INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN last_seen_at TIMESTAMPTZ NOT NULL DEFAULT now();

UPDATE batch_failed_records
SET execution_id = 0
WHERE execution_id IS NULL;

ALTER TABLE batch_failed_records
    ALTER COLUMN execution_id SET NOT NULL,
    ALTER COLUMN execution_id SET DEFAULT 0;

ALTER TABLE batch_failed_records
    ADD CONSTRAINT uq_batch_failed_records_dedupe
        UNIQUE (job_name, execution_id, step_name, phase, record_key, error_type);
