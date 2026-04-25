package com.klb.app.application.service.impl.security;

import com.klb.app.security.user.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class PostgresRlsContextAspect {

	private static final String ROLE_ADMIN = "ADMIN";
	private static final String ROLE_USER = "USER";

	private final JdbcTemplate jdbcTemplate;

	@Before("execution(public * com.klb.app.application.service.impl..*(..))")
	public void applyRlsRole(JoinPoint ignored) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String role = resolveRole(authentication);
		String username = resolveUsername(authentication);
		String dataScope = resolveDataScope(authentication);
		jdbcTemplate.queryForObject("select set_rls_context(?, ?, ?)", String.class, role, username, dataScope);
	}

	private static String resolveRole(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails)) {
			return ROLE_USER;
		}
		boolean isAdmin = authentication.getAuthorities()
				.stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch("ROLE_ADMIN"::equals);
		return isAdmin ? ROLE_ADMIN : ROLE_USER;
	}

	private static String resolveUsername(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails principal)) {
			return "";
		}
		return principal.getUsername();
	}

	private static String resolveDataScope(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails principal)) {
			return "NONE";
		}
		String dataScope = principal.snapshot().dataScope();
		return dataScope == null ? "NONE" : dataScope;
	}
}
