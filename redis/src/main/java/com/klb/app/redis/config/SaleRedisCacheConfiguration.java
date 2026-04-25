package com.klb.app.redis.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(RedisInfrastructureProperties.class)
public class SaleRedisCacheConfiguration {

	@Bean
	public CacheManager cacheManager(
			RedisConnectionFactory connectionFactory,
			RedisInfrastructureProperties redisInfrastructureProperties
	) {
		RedisSerializer<Object> valueSerializer = RedisSerializer.json();
		RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
				.computePrefixWith(cacheName -> redisInfrastructureProperties.getKeyPrefix() + ":" + cacheName + ":")
				.entryTtl(Duration.ofMinutes(5));

		Map<String, RedisCacheConfiguration> customTtls = Map.of(
				"customers:list-page",
				baseConfig.entryTtl(Duration.ofMinutes(5)),
				"customers:detail",
				baseConfig.entryTtl(Duration.ofMinutes(10)),
				"products:list-page",
				baseConfig.entryTtl(Duration.ofMinutes(10))
		);

		return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(baseConfig)
				.withInitialCacheConfigurations(customTtls)
				.build();
	}
}
