package com.klb.app.application.service.impl.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Doc topic {@code <demo.ping>.DLT} — message sau khi {@link DemoKafkaListeners#onDemoPing} that bai het retry.
 * Consumer group rieng de khong anh huong consumer chinh.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class DemoKafkaDltListener {

	@KafkaListener(
			topics = "#{@kafkaTopicFactory.topic('demo','ping').concat('.DLT')}",
			groupId = "${spring.kafka.consumer.group-id}-dlt-demo",
			properties = { "auto.offset.reset=earliest" }
	)
	public void onDemoPingDlt(
			String payload,
			Acknowledgment ack,
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
			@Header(KafkaHeaders.OFFSET) Long offset
	) {
		log.warn("[kafka-demo] DLT — topic={} offset={} payload={}", topic, offset, payload);
		ack.acknowledge();
	}
}
