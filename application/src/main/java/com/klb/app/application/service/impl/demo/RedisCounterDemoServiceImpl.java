package com.klb.app.application.service.impl.demo;

import com.klb.app.application.service.demo.RedisCounterDemoService;
import com.klb.app.application.service.demo.RedisCounterResult;
import com.klb.app.redis.support.RedisKeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisCounterDemoServiceImpl implements RedisCounterDemoService {

	private final ObjectProvider<StringRedisTemplate> stringRedisTemplate;
	private final ObjectProvider<RedisKeyFactory> redisKeyFactory;

	@Override
	public RedisCounterResult bumpGlobalDemoCounter() {
		StringRedisTemplate redis = stringRedisTemplate.getIfAvailable();
		if (redis == null) {
			return new RedisCounterResult(false, null);
		}
		String key = demoCounterKey();
		Long value = redis.opsForValue().increment(key);
		redis.expire(key, Duration.ofHours(24));
		return new RedisCounterResult(true, value);
	}

	@Override
	public RedisCounterResult getGlobalDemoCounter() {
		StringRedisTemplate redis = stringRedisTemplate.getIfAvailable();
		if (redis == null) {
			return new RedisCounterResult(false, null);
		}
		String raw = redis.opsForValue().get(demoCounterKey());
		if (raw == null || raw.isBlank()) {
			return new RedisCounterResult(true, null);
		}
		try {
			return new RedisCounterResult(true, Long.parseLong(raw.trim()));
		} catch (NumberFormatException e) {
			return new RedisCounterResult(true, null);
		}
	}

	private String demoCounterKey() {
		RedisKeyFactory keyFactory = redisKeyFactory.getIfAvailable();
		return keyFactory != null
				? keyFactory.key("demo", "global-counter")
				: "app-platform:demo:global-counter";
	}
}
