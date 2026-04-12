package com.klb.app.application.service.auth;

/**
 * Cặp JWT access + refresh sau login hoặc sau refresh.
 */
public record AccessRefreshResult(
		String accessToken,
		String refreshToken,
		long accessExpiresInSeconds,
		long refreshExpiresInSeconds
) {
}
