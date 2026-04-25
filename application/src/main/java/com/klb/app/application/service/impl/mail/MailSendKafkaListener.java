package com.klb.app.application.service.impl.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klb.app.application.service.mail.MailQueuedNotification;
import com.klb.app.persistence.entity.MailQueue;
import com.klb.app.persistence.entity.MailQueueStatus;
import com.klb.app.persistence.entity.MailTemplate;
import com.klb.app.persistence.repository.MailQueueRepository;
import com.klb.app.persistence.repository.MailTemplateRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

/**
 * Consumes {@code mail.queued} — render template, gui SMTP neu cau hinh; neu khong thi log (dev).
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class MailSendKafkaListener {

	private static final ObjectMapper JSON = new ObjectMapper();

	private final MailQueueRepository mailQueueRepository;
	private final MailTemplateRepository mailTemplateRepository;
	private final ObjectProvider<JavaMailSender> mailSenderProvider;

	@Value("${spring.application.name:app-platform}")
	private String applicationName;

	@KafkaListener(
			topics = "#{@kafkaTopicFactory.topic('mail','queued')}",
			groupId = "${spring.kafka.consumer.group-id}"
	)
	public void onMailQueued(String payload, Acknowledgment ack) {
		try {
			MailQueuedNotification n = JSON.readValue(payload, MailQueuedNotification.class);
			process(n.mailQueueId());
			ack.acknowledge();
		} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			log.warn("[mail] payload JSON khong hop le: {}", payload, e);
			ack.acknowledge();
		} catch (Exception e) {
			log.warn("[mail] consume loi payload={}", payload, e);
			throw e;
		}
	}

	private void process(java.util.UUID mailQueueId) {
		MailQueue row = mailQueueRepository.findById(mailQueueId).orElse(null);
		if (row == null) {
			log.warn("[mail] khong tim mail_queue id={}", mailQueueId);
			return;
		}
		if (row.getStatus() == MailQueueStatus.SENT) {
			return;
		}
		if (row.getStatus() == MailQueueStatus.FAILED) {
			log.debug("[mail] bo qua id={} — da FAILED", mailQueueId);
			return;
		}
		// mail_queue chỉ có template_code + variables; lấy khuôn chữ từ mail_template.
		MailTemplate template = mailTemplateRepository.findByCodeAndActiveTrue(row.getTemplateCode())
				.orElse(null);
		if (template == null) {
			row.setStatus(MailQueueStatus.FAILED);
			row.setLastError("Unknown template: " + row.getTemplateCode());
			row.setAttemptCount(row.getAttemptCount() + 1);
			mailQueueRepository.save(row);
			return;
		}

		Map<String, String> vars = row.getVariables() != null ? row.getVariables() : Map.of();
		if (!vars.containsKey("appName")) {
			vars = new java.util.HashMap<>(vars);
			vars.put("appName", applicationName);
		}

		// Thay {{...}} trước; subject/html đầy đủ mới đưa vào MimeMessage.
		String subject = MailTemplateRenderer.render(template.getSubjectTemplate(), vars);
		String html = MailTemplateRenderer.render(template.getBodyHtml(), vars);

		JavaMailSender sender = mailSenderProvider.getIfAvailable();
		if (sender == null) {
			log.info("[mail] (simulated — chua cau hinh spring.mail.host) to={} subject={}", row.getToAddress(), subject);
			markSent(row);
			return;
		}

		try {
			MimeMessage mime = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
			helper.setTo(row.getToAddress()); // từ mail_queue.to_address (snapshot lúc đăng ký)
			helper.setSubject(subject);      // đã render từ mail_template.subject_template
			helper.setText(html, true);      // đã render từ mail_template.body_html
			sender.send(mime);
			markSent(row);
			log.info("[mail] da gui to={} template={}", row.getToAddress(), row.getTemplateCode());
		} catch (Exception e) {
			row.setStatus(MailQueueStatus.FAILED);
			row.setAttemptCount(row.getAttemptCount() + 1);
			row.setLastError(trimError(e));
			mailQueueRepository.save(row);
			log.warn("[mail] gui that bai id={}", mailQueueId, e);
			throw new IllegalStateException("mail send failed", e);
		}
	}

	private void markSent(MailQueue row) {
		row.setStatus(MailQueueStatus.SENT);
		row.setSentAt(Instant.now());
		mailQueueRepository.save(row);
	}

	private static String trimError(Exception e) {
		String m = e.getMessage();
		if (m == null) {
			return e.getClass().getSimpleName();
		}
		return m.length() > 2000 ? m.substring(0, 2000) : m;
	}
}
