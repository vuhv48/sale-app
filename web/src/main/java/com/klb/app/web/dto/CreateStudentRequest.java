package com.klb.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStudentRequest(
		@NotBlank @Size(max = 32) String studentCode,
		@NotBlank @Size(max = 255) String fullName
) {
}
