package com.klb.app.batch.document;

import com.klb.app.persistence.repository.DocumentRepository;
import com.klb.app.persistence.repository.DocumentVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DocumentVersionBackfillWriter implements ItemWriter<UUID> {

	private final DocumentVersionRepository documentVersionRepository;
	private final DocumentRepository documentRepository;
	private final BatchFailedRecordLogger failedRecordLogger;

	@Override
	public void write(Chunk<? extends UUID> chunk) {
		for (UUID documentId : chunk.getItems()) {
			try {
				documentVersionRepository.insertVersionOneIfMissing(documentId);
				documentRepository.attachVersionOneAsCurrent(documentId);
			} catch (RuntimeException ex) {
				failedRecordLogger.insertFailedRecord(
						DocumentVersionBackfillJobConfig.JOB_NAME,
						null,
						"documentVersionBackfillStep",
						"WRITE",
						documentId.toString(),
						ex.getClass().getName(),
						ex.getMessage() != null ? ex.getMessage() : "No message"
				);
				if (isRetryable(ex)) {
					throw ex;
				}
				throw new IllegalArgumentException("Skip failed record: " + documentId, ex);
			}
		}
	}

	private boolean isRetryable(RuntimeException ex) {
		return ex instanceof CannotAcquireLockException
				|| ex instanceof TransientDataAccessException;
	}
}

