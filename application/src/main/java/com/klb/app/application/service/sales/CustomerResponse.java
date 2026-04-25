package com.klb.app.application.service.sales;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
		UUID id,
		String customerCode,
		String name,
		String phone,
		String email,
		String taxCode,
		String addressLine,
		boolean active,
		Instant createdAt
) {
}
