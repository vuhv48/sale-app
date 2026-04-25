package com.klb.app.redis.support;

import com.klb.app.redis.config.RedisInfrastructureProperties;
import lombok.RequiredArgsConstructor;

/**
 * Chuan hoa key: {@code prefix:domain:segment...}
 */
@RequiredArgsConstructor
public final class RedisKeyFactory {

	private final RedisInfrastructureProperties properties;

	public String key(String domain, String... segments) {
		StringBuilder sb = new StringBuilder(properties.getKeyPrefix())
				.append(':')
				.append(domain);
		for (String s : segments) {
			sb.append(':').append(s);
		}
		return sb.toString();
	}
}
