package com.klb.app.web.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class ChatWebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

	private final ChatHandshakeInterceptor chatHandshakeInterceptor;
	private final ChatPrincipalHandshakeHandler chatPrincipalHandshakeHandler;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic", "/queue");
		registry.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws/chat")
				.setHandshakeHandler(chatPrincipalHandshakeHandler)
				.addInterceptors(chatHandshakeInterceptor)
				.setAllowedOriginPatterns("*");
	}
}
