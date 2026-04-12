package com.klb.app.application.student;

import java.time.Instant;

public record StudentResponse(Long id, String studentCode, String fullName, Instant createdAt) {
}
