package com.klb.app.application.service.demo;

/** Ket qua them 1 note. */
public record MongoStudentNotesAppendResult(boolean mongoAvailable, MongoStudentNoteItem saved) {

	public static MongoStudentNotesAppendResult unavailable() {
		return new MongoStudentNotesAppendResult(false, null);
	}
}
