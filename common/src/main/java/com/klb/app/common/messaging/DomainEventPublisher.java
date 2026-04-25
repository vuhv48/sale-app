package com.klb.app.common.messaging;

import java.util.concurrent.CompletableFuture;

public interface DomainEventPublisher {

	CompletableFuture<DomainEventSendResult> publish(String topic, String key, String payload);
}
