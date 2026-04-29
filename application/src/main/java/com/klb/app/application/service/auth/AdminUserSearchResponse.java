package com.klb.app.application.service.auth;

import java.util.UUID;

/**
 * Kết quả tìm kiếm user cho màn hình quản trị.
 */
public record AdminUserSearchResponse(
		UUID id,
		String username,
		boolean enabled,
		String dataScope
) {
}
