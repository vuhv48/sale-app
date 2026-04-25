package com.klb.app.application.service.mail;

import java.util.UUID;

/**
 * Payload gui qua Kafka sau khi ghi {@code mail_queue} + {@code integration_outbox}.
 */
public record MailQueuedNotification(UUID mailQueueId) {
}
