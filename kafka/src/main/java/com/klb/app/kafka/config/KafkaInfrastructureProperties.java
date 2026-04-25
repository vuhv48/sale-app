package com.klb.app.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bat/tat Kafka o tang ung dung. Cau hinh broker / producer / consumer: {@code spring.kafka.*}.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaInfrastructureProperties {

	/** Bat Kafka: {@code true} can broker; {@code false} — khong dang ky Kafka beans. */
	private boolean enabled = false;

	/** Tien to topic (vd. {@code app-platform.demo.event}). */
	private String topicPrefix = "app-platform";
}
