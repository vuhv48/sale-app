package com.klb.app.application.service.security;

public interface AuthorizationService {

	void assertAuthority(String authority);

	void assertRequestAccess(String httpMethod, String requestPath);
}
