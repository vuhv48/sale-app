package com.klb.app.application.service.sales;

import com.klb.app.persistence.entity.SalesOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SalesOrderResponse(
		UUID id,
		String orderNo,
		UUID customerId,
		String customerCode,
		String customerName,
		SalesOrderStatus orderStatus,
		Instant orderDate,
		String note,
		BigDecimal totalAmount,
		List<SalesOrderItemResponse> items,
		Instant createdAt
) {
}
