-- Spring Batch 6+ expects batch_job_instance_seq for JOB_INSTANCE_ID.
-- V18 created batch_job_seq (older naming); JDBC metadata store uses batch_job_instance_seq.

CREATE SEQUENCE IF NOT EXISTS batch_job_instance_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

DO $$
DECLARE
    mx bigint;
BEGIN
    SELECT MAX(job_instance_id) INTO mx FROM batch_job_instance;
    IF mx IS NULL THEN
        PERFORM setval('batch_job_instance_seq', 1, false);
    ELSE
        PERFORM setval('batch_job_instance_seq', mx, true);
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'sale_app_user') THEN
        GRANT USAGE, SELECT, UPDATE ON SEQUENCE batch_job_instance_seq TO sale_app_user;
    END IF;
END
$$;
