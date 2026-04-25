package com.klb.app.application.service.demo;

import java.util.UUID;

/**
 * Demo ghi chu linh hoat trong Mongo ({@code student_notes_demo}), lien ket {@code studentId} voi Postgres logic.
 */
public interface MongoStudentNotesDemoService {

	MongoStudentNotesAppendResult append(UUID studentId, String body);

	MongoStudentNotesDemoResult listByStudent(UUID studentId);
}
