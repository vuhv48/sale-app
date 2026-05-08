package com.klb.app.application.service.impl.demo;

import com.klb.app.application.service.demo.RedisStickyNoteDemoResult;
import com.klb.app.application.service.demo.RedisStickyNoteDemoService;
import com.klb.app.redis.support.RedisKeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisStickyNoteDemoServiceImpl implements RedisStickyNoteDemoService {

	private static final Duration TTL = Duration.ofHours(24);

	private final ObjectProvider<StringRedisTemplate> stringRedisTemplate;
	private final ObjectProvider<RedisKeyFactory> redisKeyFactory;

	@Override
	public RedisStickyNoteDemoResult get() {
		StringRedisTemplate redis = stringRedisTemplate.getIfAvailable();
		if (redis == null) {
			return new RedisStickyNoteDemoResult(false, null);
		}
		String raw = redis.opsForValue().get(stickyKey());
		return new RedisStickyNoteDemoResult(true, raw);
	}

	@Override
	public RedisStickyNoteDemoResult save(String text) {
		StringRedisTemplate redis = stringRedisTemplate.getIfAvailable();
		if (redis == null) {
			return new RedisStickyNoteDemoResult(false, null);
		}
		String normalized = text != null ? text.trim() : "";
		if (normalized.isEmpty()) {
			redis.delete(stickyKey());
			return new RedisStickyNoteDemoResult(true, null);
		}
		redis.opsForValue().set(stickyKey(), normalized, TTL);
		return new RedisStickyNoteDemoResult(true, normalized);
	}

	private String stickyKey() {
		RedisKeyFactory f = redisKeyFactory.getIfAvailable();
		return f != null ? f.key("demo", "sticky-note") : "app-platform:demo:sticky-note";
	}
}
