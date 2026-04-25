package com.klb.app.application.service.sales;

import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
		UUID id,
		String productCode,
		String name,
		String description,
		boolean active,
		Instant createdAt
) {
}
