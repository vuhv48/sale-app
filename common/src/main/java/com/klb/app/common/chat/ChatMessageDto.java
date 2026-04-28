package com.klb.app.common.chat;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageDto(UUID id, String roomCode, UUID senderId, String senderUsername, String body, Instant createdAt) {
}
