package com.klb.app.batch.document;

import com.klb.app.application.batch.DocumentVersionBackfillResult;
import com.klb.app.application.batch.DocumentVersionBackfillTrigger;
import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentVersionBackfillTriggerImpl implements DocumentVersionBackfillTrigger {

	private final DocumentVersionBackfillJobRunner runner;

	@Override
	public DocumentVersionBackfillResult run(int chunkSize, int maxRounds) {
		try {
			var execution = runner.run(chunkSize, maxRounds);
			return new DocumentVersionBackfillResult(
					execution.getExitStatus().getExitCode(),
					execution.getStatus().name(),
					execution.getId(),
					execution.getStepExecutions().stream().mapToLong(se -> se.getReadCount()).sum(),
					execution.getStepExecutions().stream().mapToLong(se -> se.getWriteCount()).sum(),
					Math.max(1, chunkSize),
					Math.max(1, maxRounds)
			);
		} catch (Exception e) {
			throw new DomainException(
					ErrorStatus.BATCH_JOB_FAILED,
					e.getMessage() != null ? e.getMessage() : ErrorStatus.BATCH_JOB_FAILED.defaultMessage()
			);
		}
	}
}

