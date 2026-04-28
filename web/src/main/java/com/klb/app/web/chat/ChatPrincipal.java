package com.klb.app.web.chat;

import java.security.Principal;
import java.util.UUID;

public final class ChatPrincipal implements Principal {

	private final UUID userId;
	private final String username;

	public ChatPrincipal(UUID userId, String username) {
		this.userId = userId;
		this.username = username;
	}

	public UUID getUserId() {
		return userId;
	}

	@Override
	public String getName() {
		return username;
	}
}
