package com.klb.app.application.service.security;

public record AuthzResourceDto(
		String resourceCode,
		String resourceGroup,
		String actionCode,
		String name,
		String urlPattern,
		String httpMethod,
		boolean enabled
) {
}
