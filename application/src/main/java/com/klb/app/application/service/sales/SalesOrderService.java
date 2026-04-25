package com.klb.app.application.service.sales;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SalesOrderService {

	record CreateOrderItemInput(UUID skuId, BigDecimal quantity) {
	}

	SalesOrderPageResponse listPage(Pageable pageable);

	SalesOrderResponse getDetail(UUID orderId);

	SalesOrderResponse create(UUID customerId, String note, List<CreateOrderItemInput> items);

	SalesOrderResponse confirm(UUID orderId, String reason);

	SalesOrderResponse cancel(UUID orderId, String reason);
}
