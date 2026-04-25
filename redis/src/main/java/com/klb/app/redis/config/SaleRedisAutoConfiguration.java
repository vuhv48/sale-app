package com.klb.app.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klb.app.redis.support.RedisKeyFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Lettuce + {@link StringRedisTemplate} + {@code redisJsonTemplate}. Chi khi {@code app.redis.enabled=true}.
 * Exclude {@code DataRedisAutoConfiguration} trong YAML de Spring khong tao ket noi khi Redis tat.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "true")
@EnableConfigurationProperties({RedisInfrastructureProperties.class, DataRedisProperties.class})
public class SaleRedisAutoConfiguration {

	@Bean
	public LettuceConnectionFactory redisConnectionFactory(DataRedisProperties props) {
		RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration();
		standalone.setHostName(props.getHost());
		standalone.setPort(props.getPort());
		standalone.setDatabase(props.getDatabase());
		if (StringUtils.hasText(props.getPassword())) {
			standalone.setPassword(RedisPassword.of(props.getPassword()));
		}
		var clientBuilder = LettuceClientConfiguration.builder();
		Duration timeout = props.getTimeout();
		if (timeout != null) {
			clientBuilder.commandTimeout(timeout);
		}
		return new LettuceConnectionFactory(standalone, clientBuilder.build());
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}

	@Bean(name = "redisJsonTemplate")
	public RedisTemplate<String, Object> redisJsonTemplate(
			LettuceConnectionFactory connectionFactory,
			ObjectProvider<ObjectMapper> objectMapperProvider
	) {
		ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(SaleRedisAutoConfiguration::redisObjectMapper);
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		StringRedisSerializer stringSer = new StringRedisSerializer();
		template.setKeySerializer(stringSer);
		template.setHashKeySerializer(stringSer);
		GenericJackson2JsonRedisSerializer jsonSer = new GenericJackson2JsonRedisSerializer(objectMapper);
		template.setValueSerializer(jsonSer);
		template.setHashValueSerializer(jsonSer);
		template.afterPropertiesSet();
		return template;
	}

	@Bean
	public RedisKeyFactory redisKeyFactory(RedisInfrastructureProperties properties) {
		return new RedisKeyFactory(properties);
	}

	private static ObjectMapper redisObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}
}
