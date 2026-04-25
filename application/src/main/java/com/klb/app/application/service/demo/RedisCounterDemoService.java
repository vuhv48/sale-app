package com.klb.app.application.service.demo;

public interface RedisCounterDemoService {

	/**
	 * Tang bo dem demo trong Redis (key co TTL). Neu {@code app.redis.enabled=false} thi {@code redisAvailable=false}.
	 */
	RedisCounterResult bumpGlobalDemoCounter();

	/** Chi doc gia tri hien tai (khong INCR). Key chua co thi {@code value} null. */
	RedisCounterResult getGlobalDemoCounter();
}
