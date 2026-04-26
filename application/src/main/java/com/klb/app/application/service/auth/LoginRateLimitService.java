package com.klb.app.application.service.auth;

public interface LoginRateLimitService {

	boolean isAllowed(String clientIp);

	void clear(String clientIp);
}
