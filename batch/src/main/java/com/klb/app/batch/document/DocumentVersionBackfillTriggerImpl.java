package com.klb.app.batch.document;

import com.klb.app.application.batch.DocumentVersionBackfillResult;
import com.klb.app.application.batch.DocumentVersionBackfillTrigger;
import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
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
			Throwable root = rootCause(e);
			String rootMessage = root != null && root.getMessage() != null ? root.getMessage() : null;
			String rootType = root != null ? root.getClass().getSimpleName() : "Exception";
			log.error("[batch] documentVersionBackfillJob failed", e);
			throw new DomainException(
					ErrorStatus.BATCH_JOB_FAILED,
					rootMessage != null ? rootType + ": " + rootMessage
							: (e.getMessage() != null ? e.getMessage() : ErrorStatus.BATCH_JOB_FAILED.defaultMessage())
			);
		}
	}

	private static Throwable rootCause(Throwable t) {
		Throwable current = t;
		while (current != null && current.getCause() != null && current.getCause() != current) {
			current = current.getCause();
		}
		return current;
	}
}

