package com.klb.app.application.service.impl.demo;

import com.klb.app.application.service.demo.KafkaPingDemoService;
import com.klb.app.application.service.demo.KafkaPingResult;
import com.klb.app.kafka.support.KafkaTopicFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class KafkaPingDemoServiceImpl implements KafkaPingDemoService {

	private static final int SEND_TIMEOUT_SEC = 5;

	private final ObjectProvider<KafkaTemplate<String, String>> kafkaTemplate;
	private final ObjectProvider<KafkaTopicFactory> kafkaTopicFactory;

	@Override
	public KafkaPingResult sendPing() {
		KafkaTemplate<String, String> template = kafkaTemplate.getIfAvailable();
		KafkaTopicFactory topics = kafkaTopicFactory.getIfAvailable();
		if (template == null || topics == null) {
			return KafkaPingResult.unavailable();
		}
		String topic = topics.topic("demo", "ping");
		String key = UUID.randomUUID().toString();
		String payload = "{\"type\":\"ping\",\"key\":\"" + key + "\",\"sentAt\":\"" + Instant.now() + "\"}";
		try {
			var result = template.send(topic, key, payload).get(SEND_TIMEOUT_SEC, TimeUnit.SECONDS);
			var meta = result.getRecordMetadata();
			return new KafkaPingResult(
					true,
					meta.topic(),
					key,
					meta.partition(),
					meta.offset(),
					payload,
					null
			);
		} catch (Exception e) {
			return new KafkaPingResult(false, topic, key, null, null, payload, exceptionMessage(e));
		}
	}

	@Override
	public KafkaPingResult sendEcho() {
		KafkaTemplate<String, String> template = kafkaTemplate.getIfAvailable();
		KafkaTopicFactory topics = kafkaTopicFactory.getIfAvailable();
		if (template == null || topics == null) {
			return KafkaPingResult.unavailable();
		}
		String topic = topics.topic("demo", "echo");
		String key = UUID.randomUUID().toString();
		String payload = "{\"type\":\"echo\",\"key\":\"" + key + "\",\"sentAt\":\"" + Instant.now() + "\"}";
		try {
			var result = template.send(topic, key, payload).get(SEND_TIMEOUT_SEC, TimeUnit.SECONDS);
			var meta = result.getRecordMetadata();
			return new KafkaPingResult(
					true,
					meta.topic(),
					key,
					meta.partition(),
					meta.offset(),
					payload,
					null
			);
		} catch (Exception e) {
			return new KafkaPingResult(false, topic, key, null, null, payload, exceptionMessage(e));
		}
	}

	@Override
	public KafkaPingResult sendPingDlqDemo() {
		KafkaTemplate<String, String> template = kafkaTemplate.getIfAvailable();
		KafkaTopicFactory topics = kafkaTopicFactory.getIfAvailable();
		if (template == null || topics == null) {
			return KafkaPingResult.unavailable();
		}
		String topic = topics.topic("demo", "ping");
		String key = UUID.randomUUID().toString();
		String payload = "{\"type\":\"ping\",\"dlqDemo\":true,\"key\":\"" + key + "\",\"sentAt\":\"" + Instant.now()
				+ "\"}";
		try {
			var result = template.send(topic, key, payload).get(SEND_TIMEOUT_SEC, TimeUnit.SECONDS);
			var meta = result.getRecordMetadata();
			return new KafkaPingResult(
					true,
					meta.topic(),
					key,
					meta.partition(),
					meta.offset(),
					payload,
					null
			);
		} catch (Exception e) {
			return new KafkaPingResult(false, topic, key, null, null, payload, exceptionMessage(e));
		}
	}

	private static String exceptionMessage(Throwable e) {
		Throwable cur = e;
		while (cur.getCause() != null && cur.getCause() != cur) {
			cur = cur.getCause();
		}
		String msg = cur.getMessage();
		return (msg != null && !msg.isBlank()) ? msg : e.toString();
	}
}
