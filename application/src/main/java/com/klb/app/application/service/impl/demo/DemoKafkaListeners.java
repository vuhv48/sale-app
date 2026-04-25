package com.klb.app.application.service.impl.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Hai {@link KafkaListener} minh hoa — hai topic khac nhau (cung {@code group-id} trong YAML).
 * Chi dang ky khi {@code app.kafka.enabled=true}.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class DemoKafkaListeners {

	/** Topic: {@code KafkaTopicFactory.topic("demo","ping")} → app-platform.demo.ping */
	@KafkaListener(
			topics = "#{@kafkaTopicFactory.topic('demo','ping')}",
			groupId = "${spring.kafka.consumer.group-id}"
	)
	public void onDemoPing(String payload, Acknowledgment ack) {
		log.info("[kafka-demo] listener #1 — topic demo.ping: {}", payload);
		// Demo DLT: gui payload co "dlqDemo":true (GET /api/demo/kafka-ping-dlq) → throw → retry → topic .DLT
		if (payload != null && payload.contains("\"dlqDemo\":true")) {
			throw new IllegalStateException("intentional demo failure → DLT after retries");
		}
		ack.acknowledge();
	}

	/** Topic: {@code KafkaTopicFactory.topic("demo","echo")} → app-platform.demo.echo */
	@KafkaListener(
			topics = "#{@kafkaTopicFactory.topic('demo','echo')}",
			groupId = "${spring.kafka.consumer.group-id}"
	)
	public void onDemoEcho(String payload, Acknowledgment ack) {
		log.info("[kafka-demo] listener #2 — topic demo.echo: {}", payload);
		ack.acknowledge();
	}
}
