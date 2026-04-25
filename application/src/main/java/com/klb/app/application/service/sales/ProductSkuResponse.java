package com.klb.app.application.service.sales;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductSkuResponse(
		UUID id,
		UUID productId,
		String skuCode,
		String skuName,
		BigDecimal unitPrice,
		boolean active,
		Instant createdAt
) {
}
