package com.klb.app.application.service.sales;

import java.util.List;

public record SalesOrderPageResponse(
		List<SalesOrderResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages,
		boolean first,
		boolean last
) {
}
