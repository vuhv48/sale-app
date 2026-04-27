package com.klb.app.batch.document;

import com.klb.app.persistence.repository.DocumentRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

@Component
@StepScope
public class DocumentMissingVersionReader implements ItemReader<UUID> {

	private final DocumentRepository documentRepository;
	private final int chunkSize;
	private final long maxItems;
	private long emitted = 0;
	private final Queue<UUID> buffer = new ArrayDeque<>();

	public DocumentMissingVersionReader(
			DocumentRepository documentRepository,
			@Value("#{jobParameters['chunkSize']}") Long chunkSize,
			@Value("#{jobParameters['maxRounds']}") Long maxRounds
	) {
		this.documentRepository = documentRepository;
		this.chunkSize = Math.max(1, chunkSize == null ? 500 : chunkSize.intValue());
		long safeRounds = Math.max(1L, maxRounds == null ? 200L : maxRounds);
		this.maxItems = this.chunkSize * safeRounds;
	}

	@Override
	public synchronized UUID read() {
		if (emitted >= maxItems) {
			return null;
		}
		if (buffer.isEmpty()) {
			List<UUID> ids = documentRepository.findMissingCurrentVersionIdsByPartition(chunkSize, 1, 0);
			if (ids.isEmpty()) {
				return null;
			}
			buffer.addAll(ids);
		}
		UUID id = buffer.poll();
		if (id != null) {
			emitted++;
		}
		return id;
	}
}

