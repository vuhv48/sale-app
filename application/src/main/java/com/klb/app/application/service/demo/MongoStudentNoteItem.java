package com.klb.app.application.service.demo;

import java.time.Instant;
import java.util.UUID;

/** Mot dong ghi chu trong Mongo (demo). */
public record MongoStudentNoteItem(String id, UUID studentId, String body, Instant createdAt) {
}
