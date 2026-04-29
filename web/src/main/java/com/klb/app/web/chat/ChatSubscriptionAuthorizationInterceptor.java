package com.klb.app.web.chat;

import com.klb.app.application.service.chat.InternalChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ChatSubscriptionAuthorizationInterceptor implements ChannelInterceptor {

	private static final String CHAT_TOPIC_PREFIX = "/topic/chat/";

	private final InternalChatService internalChatService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || accessor.getCommand() != StompCommand.SUBSCRIBE) {
			return message;
		}
		String destination = accessor.getDestination();
		if (!StringUtils.hasText(destination) || !destination.startsWith(CHAT_TOPIC_PREFIX)) {
			return message;
		}
		String roomCode = destination.substring(CHAT_TOPIC_PREFIX.length()).trim();
		if (!(accessor.getUser() instanceof ChatPrincipal principal)) {
			throw new AccessDeniedException("Unauthorized websocket subscription");
		}
		if (!internalChatService.isMember(roomCode, principal.getUserId())) {
			throw new AccessDeniedException("Forbidden: you are not a member of room " + roomCode);
		}
		return message;
	}
}
