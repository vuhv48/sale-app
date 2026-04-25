package com.klb.app.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebAuthorizationConfiguration implements WebMvcConfigurer {

	private final RequestAuthorizationInterceptor requestAuthorizationInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(requestAuthorizationInterceptor)
				.addPathPatterns("/**");
	}
}
