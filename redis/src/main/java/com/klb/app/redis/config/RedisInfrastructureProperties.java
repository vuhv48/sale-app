package com.klb.app.redis.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cau hinh ung dung Redis (ket noi: {@code spring.data.redis.*}).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.redis")
public class RedisInfrastructureProperties {

	/** Bat Redis: {@code true} can server Redis; {@code false} app chay khong Redis. */
	private boolean enabled = false;

	/** Tien to key tren cluster. */
	private String keyPrefix = "app-platform";
}
