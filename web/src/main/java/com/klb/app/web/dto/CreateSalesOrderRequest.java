package com.klb.app.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateSalesOrderRequest(
		@NotNull UUID customerId,
		String note,
		@NotEmpty List<@Valid Item> items
) {
	public record Item(
			@NotNull UUID skuId,
			@NotNull @DecimalMin("0.001") BigDecimal quantity
	) {
	}
}
