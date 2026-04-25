package com.klb.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MongoStudentNoteAppendRequest(
		@NotNull UUID studentId,
		@NotBlank String body
) {
}
