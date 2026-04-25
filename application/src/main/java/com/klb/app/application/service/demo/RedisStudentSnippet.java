package com.klb.app.application.service.demo;

import java.util.UUID;

/** Payload demo luu Redis duoi dang JSON (khong thay cho DB). */
public record RedisStudentSnippet(UUID id, String studentCode, String fullName) {
}
