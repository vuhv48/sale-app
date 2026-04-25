package com.klb.app.web.dto;

import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
		@Size(max = 255) String name,
		@Size(max = 32) String phone,
		@Size(max = 255) String email,
		@Size(max = 64) String taxCode,
		@Size(max = 500) String addressLine,
		Boolean active
) {
}
