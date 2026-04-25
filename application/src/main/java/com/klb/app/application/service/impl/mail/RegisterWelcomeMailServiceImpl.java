package com.klb.app.application.service.impl.mail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klb.app.application.service.mail.MailQueuedNotification;
import com.klb.app.application.service.mail.RegisterWelcomeMailService;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterWelcomeMailServiceImpl implements RegisterWelcomeMailService {

	public static final String TEMPLATE_WELCOME_REGISTER = "WELCOME_REGISTER";

	private static final ObjectMapper JSON = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	private final MailQueueRepository mailQueueRepository;
	private final IntegrationOutboxRepository integrationOutboxRepository;
	private final ObjectProvider<KafkaTopicFactory> kafkaTopicFactory;

	@Override
	@Transactional
	public void enqueueWelcomeEmail(UUID userId, String username, String contactEmail) {
		String to = resolveToAddress(contactEmail, username);
		if (to == null) {
			log.debug("[mail] bo qua welcome — khong co dia chi hop le (username={})", username);
			return;
		}
		String idempotencyKey = "welcome:register:" + userId;
		if (mailQueueRepository.existsByIdempotencyKey(idempotencyKey)) {
			return;
		}

		// Biến cho {{username}}, {{appName}} trong mail_template — không lưu subject/body đã render.
		Map<String, String> variables = new HashMap<>();
		variables.put("username", username);
		variables.put("appName", "Sale App");

		MailQueue row = new MailQueue();
		row.setUserId(userId);
		row.setToAddress(to);
		row.setTemplateCode(TEMPLATE_WELCOME_REGISTER);
		row.setVariables(variables);
		row.setStatus(MailQueueStatus.PENDING);
		row.setIdempotencyKey(idempotencyKey);
		mailQueueRepository.save(row);

		KafkaTopicFactory topics = kafkaTopicFactory.getIfAvailable();
		if (topics == null) {
			log.debug("[mail] Kafka tat — chi luu mail_queue id={}, can job SMTP sau", row.getId());
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

	static String resolveToAddress(String contactEmail, String username) {
		if (StringUtils.hasText(contactEmail)) {
			String e = contactEmail.trim();
			if (e.contains("@")) {
				return e;
			}
		}
		if (StringUtils.hasText(username) && username.trim().contains("@")) {
			return username.trim();
		}
		return null;
	}
}
