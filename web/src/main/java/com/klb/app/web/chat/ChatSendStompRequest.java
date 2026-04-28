package com.klb.app.web.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatSendStompRequest(String roomCode, String body) {

	public ChatSendStompRequest {
		roomCode = roomCode != null ? roomCode.trim() : "";
		body = body != null ? body.trim() : "";
	}
}
