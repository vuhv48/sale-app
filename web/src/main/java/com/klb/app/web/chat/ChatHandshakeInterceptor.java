package com.klb.app.web.chat;

import com.klb.app.security.jwt.JwtService;
import com.klb.app.security.user.AppUserDetails;
import com.klb.app.security.user.AppUserDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

	private final JwtService jwtService;
	private final AppUserDetailsService userDetailsService;

	@Override
	public boolean beforeHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Map<String, Object> attributes
	) {
		if (!(request instanceof ServletServerHttpRequest servletRequest)) {
			return false;
		}
		HttpServletRequest nativeRequest = servletRequest.getServletRequest();
		String token = nativeRequest.getParameter("token");
		if (!StringUtils.hasText(token)) {
			return false;
		}
		try {
			Claims claims = jwtService.parseClaims(token.trim());
			String username = claims.getSubject();
			if (!StringUtils.hasText(username)) {
				return false;
			}
			var user = userDetailsService.loadUserByUsername(username);
			if (!(user instanceof AppUserDetails details)) {
				return false;
			}
			if (!details.isEnabled()) {
				return false;
			}
			attributes.put(ChatWsAttributes.CHAT_PRINCIPAL, new ChatPrincipal(details.getId(), details.getUsername()));
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
	}
}
