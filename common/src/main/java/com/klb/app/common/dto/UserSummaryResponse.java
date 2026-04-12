package com.klb.app.common.dto;

import java.util.Set;

/** Read model cho GET /api/users/current — đặt ở common để web/security đều dùng được, không phụ thuộc persistence trong web. */
public record UserSummaryResponse(
		Long id,
		String username,
		String dataScope,
		Set<String> roles,
		Set<String> permissions
) {
}
