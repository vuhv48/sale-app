package com.klb.app.application.service.impl.sales.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klb.app.application.service.sales.sync.CustomerSyncEvent;
import lombok.extern.slf4j.Slf4j;
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

		List<CustomerSyncEvent> events = new ArrayList<>(payloads.size());
		for (String payload : payloads) {
			events.add(parse(payload));
		}

		upsertService.bulkUpsert(events);
		ack.acknowledge();
	}

	private CustomerSyncEvent parse(String payload) {
		try {
			return OBJECT_MAPPER.readValue(payload, CustomerSyncEvent.class);
		} catch (JsonProcessingException e) {
			// Parse loi coi nhu data-bad -> cho retry/DLT (de khong nuot mat du lieu)
			throw new IllegalArgumentException("Invalid customer sync payload: " + payload, e);
		}
	}
}

