package com.klb.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank @Size(min = 3, max = 64) String username,
		@NotBlank @Size(min = 8, max = 128) String password,
		/** Email nhan mail chao; co the de trong neu dung username dang email. */
		@Size(max = 320) String email
) {
}
