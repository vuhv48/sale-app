package com.klb.app.web.chat;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class ChatPrincipalHandshakeHandler extends DefaultHandshakeHandler {

	@Override
	protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
		Object p = attributes.get(ChatWsAttributes.CHAT_PRINCIPAL);
		if (p instanceof Principal principal) {
			return principal;
		}
		return super.determineUser(request, wsHandler, attributes);
	}
}
