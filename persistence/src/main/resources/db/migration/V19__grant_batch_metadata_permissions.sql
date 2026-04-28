DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'sale_app_user') THEN
        GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE
            batch_job_instance,
            batch_job_execution,
            batch_job_execution_params,
            batch_step_execution,
            batch_step_execution_context,
            batch_job_execution_context
        TO sale_app_user;

        GRANT USAGE, SELECT, UPDATE ON SEQUENCE
            batch_job_seq,
            batch_job_execution_seq,
            batch_step_execution_seq
        TO sale_app_user;
    END IF;
END
$$;
