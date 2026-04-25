package com.klb.app.web.dto;

import jakarta.validation.constraints.Size;

public record UpdateOrderStatusRequest(
		@Size(max = 500) String reason
) {
}
