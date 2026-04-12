package com.klb.app.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Các route được phép không cần JWT — khai báo trong {@code application.yaml} ({@code app.security.permit-all}).
 */
@ConfigurationProperties(prefix = "app.security")
public record SecurityPermitProperties(List<PermitPath> permitAll) {

	public SecurityPermitProperties {
		permitAll = permitAll != null ? List.copyOf(permitAll) : List.of();
	}

	public record PermitPath(String method, String path) {
	}
}
