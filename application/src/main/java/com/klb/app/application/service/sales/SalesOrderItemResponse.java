package com.klb.app.application.service.sales;

import java.math.BigDecimal;
import java.util.UUID;

public record SalesOrderItemResponse(
		UUID id,
		UUID skuId,
		String skuCode,
		String skuName,
		BigDecimal quantity,
		BigDecimal unitPrice,
		BigDecimal lineTotal
) {
}
