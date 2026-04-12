package com.klb.app.application.service.student;

import java.time.Instant;
import java.util.UUID;

/**
 * Tham chiếu sinh viên vừa import (không phải entity JPA) — dùng cho batch / log sau khi insert.
 */
public record ImportedStudentRef(UUID id, String studentCode, String fullName, Instant createdAt) {
}
