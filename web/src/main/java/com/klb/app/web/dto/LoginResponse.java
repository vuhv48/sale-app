package com.klb.app.web.dto;

public record LoginResponse(
		String accessToken,
		String refreshToken,
		String tokenType,
		long expiresInSeconds,
		long refreshExpiresInSeconds
) {
}
