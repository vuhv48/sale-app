package com.klb.app.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateSkuRequest(
		@Size(max = 255) String skuName,
		@DecimalMin("0.0") BigDecimal unitPrice,
		Boolean active
) {
}
