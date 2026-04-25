package com.klb.app.application.service.impl.auth;

import com.klb.app.application.service.auth.LoginRateLimitService;
import com.klb.app.redis.support.RedisKeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginRateLimitServiceImpl implements LoginRateLimitService {

	private static final long MAX_REQUESTS_PER_WINDOW = 5;
	private static final Duration WINDOW = Duration.ofMinutes(1);

	private final ObjectProvider<StringRedisTemplate> stringRedisTemplate;
	private final ObjectProvider<RedisKeyFactory> redisKeyFactory;

	@Override
	public boolean isAllowed(String clientIp) {
		StringRedisTemplate redis = stringRedisTemplate.getIfAvailable();
		if (redis == null) {
			return true;
		}
		String key = loginRateKey(clientIp);
		Long count = redis.opsForValue().increment(key);
		if (count == null) {
			return true;
		}
		if (count == 1L) {
			redis.expire(key, WINDOW);
		}
		return count <= MAX_REQUESTS_PER_WINDOW;
	}

	@Override
	public void clear(String clientIp) {
		StringRedisTemplate redis = stringRedisTemplate.getIfAvailable();
		if (redis == null) {
			return;
		}
		redis.delete(loginRateKey(clientIp));
	}

	private String loginRateKey(String clientIp) {
		String safeIp = (clientIp == null || clientIp.isBlank())
				? "unknown"
				: clientIp.trim().replace(':', '_');
		RedisKeyFactory keyFactory = redisKeyFactory.getIfAvailable();
		return keyFactory != null
				? keyFactory.key("auth", "login-rate", safeIp)
				: "app-platform:auth:login-rate:" + safeIp;
	}
}
