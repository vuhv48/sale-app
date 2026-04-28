package com.klb.app.common.chat;

import java.time.Instant;
import java.util.UUID;

public record ChatRoomDto(
		UUID id,
		String code,
		String name,
		String roomType,
		Instant createdAt
) {
}
