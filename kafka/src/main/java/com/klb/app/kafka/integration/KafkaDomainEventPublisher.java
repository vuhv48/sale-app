package com.klb.app.kafka.integration;

import com.klb.app.common.messaging.DomainEventPublisher;
import com.klb.app.common.messaging.DomainEventSendResult;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * Gui payload string len Kafka; topic/key do tang application quyet dinh.
 */
public final class KafkaDomainEventPublisher implements DomainEventPublisher {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaDomainEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public CompletableFuture<DomainEventSendResult> publish(String topic, String key, String payload) {
		return kafkaTemplate.send(topic, key, payload).thenApply(result -> {
			var meta = result.getRecordMetadata();
			return new DomainEventSendResult(meta.topic(), meta.partition(), meta.offset());
		});
	}
}
