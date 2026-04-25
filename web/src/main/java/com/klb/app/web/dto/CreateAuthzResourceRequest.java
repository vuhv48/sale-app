package com.klb.app.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAuthzResourceRequest(
		@NotBlank String resourceCode,
		@NotBlank String resourceGroup,
		@NotBlank String actionCode,
		@NotBlank String name,
		@NotBlank String urlPattern,
		String httpMethod
) {
}
