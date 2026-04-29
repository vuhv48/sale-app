package com.klb.app.web.security;

import com.klb.app.application.service.security.AuthorizationService;
import com.klb.app.security.config.SecurityPermitProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RequestAuthorizationInterceptor implements HandlerInterceptor {

	private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

	private final AuthorizationService authorizationService;
	private final SecurityPermitProperties permitProperties;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String method = request.getMethod();
		if ("OPTIONS".equalsIgnoreCase(method)) {
			return true;
		}
		String requestUri = request.getRequestURI();
		String contextPath = request.getContextPath();
		String appPath = requestUri;
		if (StringUtils.hasText(contextPath) && requestUri.startsWith(contextPath)) {
			appPath = requestUri.substring(contextPath.length());
		}
		if (isPermitAll(method, appPath)) {
			return true;
		}
		authorizationService.assertRequestAccess(method, appPath);
		return true;
	}

	private boolean isPermitAll(String requestMethod, String requestPath) {
		return permitProperties.permitAll().stream()
				.anyMatch(p -> methodMatches(requestMethod, p.method()) && PATH_MATCHER.match(p.path(), requestPath));
	}

	private static boolean methodMatches(String requestMethod, String configuredMethod) {
		return StringUtils.hasText(configuredMethod) && configuredMethod.equalsIgnoreCase(requestMethod);
	}
}
