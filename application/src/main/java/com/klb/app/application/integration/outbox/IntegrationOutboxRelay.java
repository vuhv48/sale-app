package com.klb.app.application.integration.outbox;

import com.klb.app.persistence.entity.OutboxStatus;
import com.klb.app.persistence.repository.IntegrationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Poll bang {@code integration_outbox} va gui Kafka. Chi chay khi {@code app.kafka.enabled=true}.
 */
@Slf4j
@Component
@ConditionalOnProperty(
		prefix = "app.kafka",
		name = {"enabled", "outbox.relay-enabled"},
		havingValue = "true",
		matchIfMissing = true)
@RequiredArgsConstructor
public class IntegrationOutboxRelay {

	private final IntegrationOutboxRepository outboxRepository;
	private final IntegrationOutboxDispatchService dispatchService;

	@Value("${app.kafka.outbox.batch-size:50}")
	private int batchSize;

	@Scheduled(fixedDelayString = "${app.kafka.outbox.publish-interval-ms:2000}")
	public void relayPending() {
		var ids = outboxRepository.findIdsByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, PageRequest.of(0, batchSize));
		for (UUID id : ids) {
			try {
				dispatchService.publishOne(id);
			} catch (Exception e) {
				log.warn("[outbox] unexpected error id={}", id, e);
			}
		}
	}
}
