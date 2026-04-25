package com.klb.app.application.service.sales;

import java.util.List;

public record SalesSimplePageResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages,
		boolean first,
		boolean last
) {
}
