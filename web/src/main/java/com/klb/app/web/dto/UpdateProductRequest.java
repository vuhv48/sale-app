package com.klb.app.web.dto;

import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
		@Size(max = 255) String name,
		String description,
		Boolean active
) {
}
