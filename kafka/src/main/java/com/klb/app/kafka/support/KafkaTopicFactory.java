package com.klb.app.kafka.support;

import com.klb.app.kafka.config.KafkaInfrastructureProperties;
import lombok.RequiredArgsConstructor;

/**
 * Chuan hoa ten topic: {@code prefix.segment1.segment2...}
 */
@RequiredArgsConstructor
public final class KafkaTopicFactory {

	private final KafkaInfrastructureProperties properties;

	public String topic(String first, String... more) {
		StringBuilder sb = new StringBuilder(properties.getTopicPrefix()).append('.').append(first);
		for (String s : more) {
			sb.append('.').append(s);
		}
		return sb.toString();
	}
}
