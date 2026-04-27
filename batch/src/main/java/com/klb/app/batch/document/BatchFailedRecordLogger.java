package com.klb.app.batch.document;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchFailedRecordLogger {

	private final JdbcTemplate jdbcTemplate;

	public void insertFailedRecord(
			String jobName,
			Long executionId,
			String stepName,
			String phase,
			String recordKey,
			String errorType,
			String errorMessage
	) {
		jdbcTemplate.update("""
				INSERT INTO batch_failed_records (
				    job_name, execution_id, step_name, phase, record_key, error_type, error_message, payload
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb)
				ON CONFLICT (job_name, execution_id, step_name, phase, record_key, error_type)
				DO UPDATE
				   SET retry_count = batch_failed_records.retry_count + 1,
				       error_message = EXCLUDED.error_message,
				       last_seen_at = now()
				""",
				jobName,
				executionId != null ? executionId : 0L,
				stepName,
				phase,
				recordKey,
				errorType,
				errorMessage,
				null
		);
	}
}

