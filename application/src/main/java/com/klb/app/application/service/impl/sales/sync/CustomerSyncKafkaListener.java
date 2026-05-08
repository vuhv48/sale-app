package com.klb.app.application.service.impl.sales.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klb.app.application.service.sales.sync.CustomerSyncEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Consume "2tr record" theo batch:
 * - 1 record = 1 message (JSON String)
 * - Listener doc theo batch (List<String>) -> parse -> bulk upsert -> ack offset
 * Neu throw truoc ack: DefaultErrorHandler se retry va day sang <topic>.DLT khi het so lan.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class CustomerSyncKafkaListener {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
	private final CustomerSyncUpsertService upsertService;
	@Value("${app.kafka.customer-sync.fallback-chunk-size:50}")
	private int fallbackChunkSize;

	public CustomerSyncKafkaListener(CustomerSyncUpsertService upsertService) {
		this.upsertService = upsertService;
	}

	@KafkaListener(
			topics = "#{@kafkaTopicFactory.topic('sales','customer-sync')}",
			groupId = "${spring.kafka.consumer.group-id}",
			containerFactory = "kafkaBatchListenerContainerFactory",
			concurrency = "${app.kafka.customer-sync.concurrency:6}"
	)
	public void onCustomerSyncBatch(List<String> payloads, Acknowledgment ack) {
		if (payloads == null || payloads.isEmpty()) {
			ack.acknowledge();
			return;
		}

		if(true) {
			throw new IllegalStateException("force retry for test");
		}


		List<CustomerSyncEvent> events = new ArrayList<>(payloads.size());
		for (String payload : payloads) {
			events.add(parse(payload));
		}

		int successCount = processWithChunkFallback(events);
		ack.acknowledge();
		log.info("[customer-sync] batch processed: input={}, success={}", payloads.size(), successCount);
	}

	private int processWithChunkFallback(List<CustomerSyncEvent> events) {
		if (events.isEmpty()) {
			return 0;
		}
		int success = 0;
		for (int i = 0; i < events.size(); i += fallbackChunkSize) {
			int end = Math.min(i + fallbackChunkSize, events.size());
			List<CustomerSyncEvent> chunk = events.subList(i, end);
			try {
				upsertService.bulkUpsert(chunk);
				success += chunk.size();
			} catch (Exception bulkEx) {
				log.warn("[customer-sync] chunk bulk upsert failed (size={}), fallback one-by-one", chunk.size(), bulkEx);
				for (CustomerSyncEvent event : chunk) {
					try {
						upsertService.upsertOne(event);
						success++;
					} catch (Exception oneEx) {
						// Khong nuot loi: throw de DefaultErrorHandler retry va khi het retry se day sang DLT
						throw new IllegalStateException("Invalid record customerCode=" + event.customerCode(), oneEx);
					}
				}
			}
		}
		return success;
	}

	private CustomerSyncEvent parse(String payload) {
		try {
			return OBJECT_MAPPER.readValue(payload, CustomerSyncEvent.class);
		} catch (JsonProcessingException e) {
			// Parse loi -> throw de retry/DLT giu duoc error headers
			throw new IllegalArgumentException("Invalid customer sync payload: " + payload, e);
		}
	}
}

