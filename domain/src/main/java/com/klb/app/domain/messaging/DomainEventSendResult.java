package com.klb.app.domain.messaging;

/**
 * Metadata sau khi broker nhan ban ghi (dung cho logging / demo; khong phu thuoc Spring Kafka).
 */
public record DomainEventSendResult(String topic, int partition, long offset) {
}
