package com.klb.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ChatCreateGroupRoomRequest(
		@NotBlank @Size(max = 64) String code,
		@NotBlank @Size(max = 255) String name,
		List<UUID> memberUserIds
) {
}
