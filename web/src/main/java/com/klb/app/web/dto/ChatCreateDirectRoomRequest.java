package com.klb.app.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChatCreateDirectRoomRequest(@NotNull UUID peerUserId) {
}
