package com.klb.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
		@NotBlank @Size(max = 32) String productCode,
		@NotBlank @Size(max = 255) String name,
		String description
) {
}
