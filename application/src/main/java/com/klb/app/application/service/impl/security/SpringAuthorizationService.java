package com.klb.app.application.service.impl.security;

import com.klb.app.application.service.security.AuthorizationService;
import com.klb.app.persistence.repository.AuthzResourceRepository;
import com.klb.app.security.user.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpringAuthorizationService implements AuthorizationService {

	private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

	private final AuthzResourceRepository authzResourceRepository;
	@Value("${app.authz.dynamic.default-deny:true}")
	private boolean defaultDenyWhenResourceNotMapped;

	@Override
	public void assertAuthority(String authority) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getAuthorities() == null) {
			throw new AccessDeniedException("Forbidden");
		}
		boolean granted = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch(authority::equals);
		if (!granted) {
			throw new AccessDeniedException("Forbidden: missing authority " + authority);
		}
	}

	@Override
	public void assertRequestAccess(String httpMethod, String requestPath) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails principal)) {
			return;
		}
		if (!StringUtils.hasText(httpMethod) || !StringUtils.hasText(requestPath)) {
			return;
		}
		List<AuthzResourceRepository.ResourceRuleProjection> requestedResources = authzResourceRepository
				.findAllActiveResourceRules()
				.stream()
				.filter(r -> methodMatches(httpMethod, r.getHttpMethod()))
				.filter(r -> PATH_MATCHER.match(r.getUrlPattern(), requestPath))
				.toList();
		if (requestedResources.isEmpty()) {
			if (defaultDenyWhenResourceNotMapped) {
				throw new AccessDeniedException(
						"Forbidden: no dynamic resource mapping for " + httpMethod.toUpperCase() + " " + requestPath);
			}
			return;
		}
		Set<String> allowedResourceCodes = authzResourceRepository.findActiveResourceCodesByUserId(principal.getId())
				.stream()
				.collect(Collectors.toSet());
		boolean hasAnyAllowedResource = requestedResources.stream()
				.map(AuthzResourceRepository.ResourceRuleProjection::getResourceCode)
				.anyMatch(allowedResourceCodes::contains);
		if (!hasAnyAllowedResource) {
			throw new AccessDeniedException(
					"Forbidden: missing resource grant for " + httpMethod.toUpperCase() + " " + requestPath);
		}
	}

	private static boolean methodMatches(String requestMethod, String ruleMethod) {
		if (!StringUtils.hasText(ruleMethod)) {
			return true;
		}
		return ruleMethod.equalsIgnoreCase(requestMethod);
	}

}
