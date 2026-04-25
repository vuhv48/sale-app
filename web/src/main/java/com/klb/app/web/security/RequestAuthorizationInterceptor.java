package com.klb.app.web.security;

import com.klb.app.application.service.security.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RequestAuthorizationInterceptor implements HandlerInterceptor {

	private final AuthorizationService authorizationService;

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
		authorizationService.assertRequestAccess(method, appPath);
		return true;
	}
}
