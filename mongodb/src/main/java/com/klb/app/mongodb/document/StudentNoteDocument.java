package com.klb.app.mongodb.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * Ghi chu mo rong cho sinh vien — luu Mongo (khong thay Student Postgres).
 */
@Document(collection = "student_notes_demo")
@Getter
@Setter
public class StudentNoteDocument {

	@Id
	private String id;

	private UUID studentId;

	private String body;

	private Instant createdAt = Instant.now();
}
