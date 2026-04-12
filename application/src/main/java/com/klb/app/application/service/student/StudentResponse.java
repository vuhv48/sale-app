package com.klb.app.application.service.student;

import java.time.Instant;
import java.util.UUID;

public record StudentResponse(UUID id, String studentCode, String fullName, Instant createdAt) {
}
