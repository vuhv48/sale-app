package com.klb.app.application.service.impl.mail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klb.app.application.service.mail.MailQueuedNotification;
import com.klb.app.application.service.mail.OrderCreatedMailService;
import com.klb.app.kafka.support.KafkaTopicFactory;
import com.klb.app.persistence.entity.IntegrationOutbox;
import com.klb.app.persistence.entity.MailQueue;
import com.klb.app.persistence.entity.MailQueueStatus;
import com.klb.app.persistence.repository.IntegrationOutboxRepository;
import com.klb.app.persistence.repository.MailQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreatedMailServiceImpl implements OrderCreatedMailService {

	public static final String TEMPLATE_ORDER_CREATED = "ORDER_CREATED";

	private static final ObjectMapper JSON = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	private final MailQueueRepository mailQueueRepository;
	private final IntegrationOutboxRepository integrationOutboxRepository;
	private final ObjectProvider<KafkaTopicFactory> kafkaTopicFactory;

	@Override
	@Transactional
	public void enqueueOrderCreatedEmail(
			UUID orderId,
			String orderNo,
			String customerName,
			String customerEmail,
			BigDecimal totalAmount
	) {
		String to = resolveToAddress(customerEmail);
		if (to == null) {
			log.debug("[mail] bo qua ORDER_CREATED — khách không có email (orderId={}, orderNo={})", orderId, orderNo);
			return;
		}
		String idempotencyKey = "order:created:mail:" + orderId;
		if (mailQueueRepository.existsByIdempotencyKey(idempotencyKey)) {
			return;
		}

		Map<String, String> variables = new HashMap<>();
		variables.put("customerName", customerName != null ? customerName : "");
		variables.put("orderNo", orderNo != null ? orderNo : "");
		variables.put("orderId", orderId.toString());
		variables.put("totalAmount", formatAmount(totalAmount));
		variables.put("appName", "Sale App");

		MailQueue row = new MailQueue();
		row.setUserId(null);
		row.setToAddress(to);
		row.setTemplateCode(TEMPLATE_ORDER_CREATED);
		row.setVariables(variables);
		row.setStatus(MailQueueStatus.PENDING);
		row.setIdempotencyKey(idempotencyKey);
		mailQueueRepository.save(row);

		KafkaTopicFactory topics = kafkaTopicFactory.getIfAvailable();
		if (topics == null) {
			log.debug("[mail] Kafka tat — chi luu mail_queue id={} (ORDER_CREATED)", row.getId());
			return;
		}
		try {
			String topic = topics.topic("mail", "queued");
			String payload = JSON.writeValueAsString(new MailQueuedNotification(row.getId()));
			integrationOutboxRepository.save(new IntegrationOutbox(topic, row.getId().toString(), payload));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Cannot serialize MailQueuedNotification", e);
		}
	}

	private static String resolveToAddress(String contactEmail) {
		if (!StringUtils.hasText(contactEmail)) {
			return null;
		}
		String e = contactEmail.trim();
		return e.contains("@") ? e : null;
	}

	private static String formatAmount(BigDecimal totalAmount) {
		if (totalAmount == null) {
			return "0";
		}
		return totalAmount.stripTrailingZeros().toPlainString();
	}
}
