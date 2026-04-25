package com.klb.app.common.messaging;

public record DomainEventSendResult(String topic, int partition, long offset) {
}
