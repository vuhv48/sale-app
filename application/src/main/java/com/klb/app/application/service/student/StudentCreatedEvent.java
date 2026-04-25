package com.klb.app.application.service.student;

import java.time.Instant;
import java.util.UUID;

/** Su kien domain: student da duoc luu DB; dung de day len Kafka sau khi commit. */
public record StudentCreatedEvent(UUID id, String studentCode, String fullName, Instant createdAt) {
}
