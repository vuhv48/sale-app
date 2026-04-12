package com.klb.app.domain.security;

import java.util.Set;

/**
 * Ảnh chụp dữ liệu user phục vụ xác thực / JWT — không phụ thuộc JPA.
 */
public record UserSecuritySnapshot(
		Long id,
		String username,
		String passwordHash,
		boolean enabled,
		String dataScope,
		Set<String> roleCodes,
		Set<String> effectivePermissionCodes
) {
}
