package com.klb.app.application.service.demo;

import java.util.List;

public record MongoStudentNotesDemoResult(boolean mongoAvailable, List<MongoStudentNoteItem> notes) {

	public static MongoStudentNotesDemoResult unavailable() {
		return new MongoStudentNotesDemoResult(false, List.of());
	}
}
