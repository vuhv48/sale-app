package com.klb.app.application.integration.outbox;

import com.klb.app.domain.messaging.DomainEventPublisher;
import com.klb.app.persistence.entity.IntegrationOutbox;
import com.klb.app.persistence.entity.OutboxStatus;
import com.klb.app.persistence.repository.IntegrationOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Moi ban ghi outbox xu ly trong transaction rieng (REQUIRES_NEW) de mot ban loi khong rollback ban khac.
 */
@Service
@RequiredArgsConstructor
public class IntegrationOutboxDispatchService {

	private static final int SEND_TIMEOUT_SEC = 15;

	private final IntegrationOutboxRepository outboxRepository;
	private final ObjectProvider<DomainEventPublisher> domainEventPublisher;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void publishOne(UUID id) {
		var publisher = domainEventPublisher.getIfAvailable();
		if (publisher == null) {
			return;
		}
		IntegrationOutbox row = outboxRepository.findById(id).orElse(null);
		if (row == null || row.getStatus() != OutboxStatus.PENDING) {
			return;
		}
		try {
			publisher.publish(row.getTopic(), row.getMessageKey(), row.getPayload()).get(SEND_TIMEOUT_SEC, TimeUnit.SECONDS);
			row.setStatus(OutboxStatus.SENT);
			row.setSentAt(Instant.now());
			row.setLastError(null);
		} catch (Exception e) {
			String msg = e.getMessage();
			if (msg == null || msg.isBlank()) {
				msg = e.getClass().getSimpleName();
			}
			if (msg.length() > 2000) {
				msg = msg.substring(0, 2000);
			}
			row.setLastError(msg);
		}
		outboxRepository.save(row);
	}
}
