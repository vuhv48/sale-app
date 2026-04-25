package com.klb.app.domain.messaging;

import java.util.concurrent.CompletableFuture;

/**
 * Port xuat su kien ra message bus. Trien khai: module {@code kafka} khi {@code app.kafka.enabled=true}.
 */
public interface DomainEventPublisher {

	CompletableFuture<DomainEventSendResult> publish(String topic, String key, String payload);
}
