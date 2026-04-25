package com.klb.app.application.service.demo;

/**
 * Ket qua demo gui message len Kafka (producer). Consumer log o {@link com.klb.app.application.service.impl.demo.DemoKafkaListeners}.
 */
public record KafkaPingResult(
		boolean kafkaAvailable,
		String topic,
		String messageKey,
		Integer partition,
		Long offset,
		String payloadSent,
		String error
) {
	public KafkaPingResult {
		if (error != null && !error.isBlank()) {
			kafkaAvailable = false;
		}
	}

	public static KafkaPingResult unavailable() {
		return new KafkaPingResult(false, null, null, null, null, null, null);
	}
}
