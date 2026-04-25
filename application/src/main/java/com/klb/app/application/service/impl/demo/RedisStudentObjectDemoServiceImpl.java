package com.klb.app.application.service.impl.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klb.app.application.service.demo.RedisStudentObjectDemoResult;
import com.klb.app.application.service.demo.RedisStudentObjectDemoService;
import com.klb.app.application.service.demo.RedisStudentSnippet;
import com.klb.app.redis.support.RedisKeyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisStudentObjectDemoServiceImpl implements RedisStudentObjectDemoService {

	private static final ObjectMapper JSON = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	private static final Duration TTL = Duration.ofHours(1);

	private final ObjectProvider<StringRedisTemplate> stringRedisTemplate;
	private final ObjectProvider<RedisKeyFactory> redisKeyFactory;

	@Override
	public RedisStudentObjectDemoResult save(RedisStudentSnippet snippet) {
		if (snippet.id() == null || snippet.studentCode() == null || snippet.studentCode().isBlank()) {
			throw new IllegalArgumentException("Redis demo: id va studentCode bat buoc");
		}
		StringRedisTemplate redis = stringRedisTemplate.getIfAvailable();
		if (redis == null) {
			return new RedisStudentObjectDemoResult(false, null);
		}
		try {
			String json = JSON.writeValueAsString(snippet);
			String entityKey = entityKey(snippet.id());
			String codeKey = codeIndexKey(snippet.studentCode());
			redis.opsForValue().set(entityKey, json, TTL);
			redis.opsForValue().set(codeKey, snippet.id().toString(), TTL);
			return new RedisStudentObjectDemoResult(true, snippet);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Redis demo: cannot serialize snippet", e);
		}
	}

	@Override
	public RedisStudentObjectDemoResult getById(UUID id) {
		StringRedisTemplate redis = stringRedisTemplate.getIfAvailable();
		if (redis == null) {
			return new RedisStudentObjectDemoResult(false, null);
		}
		String raw = redis.opsForValue().get(entityKey(id));
		return new RedisStudentObjectDemoResult(true, parseOrNull(raw));
	}

	@Override
	public RedisStudentObjectDemoResult getByStudentCode(String studentCode) {
		StringRedisTemplate redis = stringRedisTemplate.getIfAvailable();
		if (redis == null) {
			return new RedisStudentObjectDemoResult(false, null);
		}
		if (studentCode == null || studentCode.isBlank()) {
			return new RedisStudentObjectDemoResult(true, null);
		}
		String idStr = redis.opsForValue().get(codeIndexKey(studentCode.trim()));
		if (idStr == null || idStr.isBlank()) {
			return new RedisStudentObjectDemoResult(true, null);
		}
		try {
			return getById(UUID.fromString(idStr.trim()));
		} catch (IllegalArgumentException e) {
			return new RedisStudentObjectDemoResult(true, null);
		}
	}

	private RedisStudentSnippet parseOrNull(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return JSON.readValue(raw, RedisStudentSnippet.class);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	private String entityKey(UUID id) {
		RedisKeyFactory f = redisKeyFactory.getIfAvailable();
		return f != null
				? f.key("demo", "student-json", id.toString())
				: "app-platform:demo:student-json:" + id;
	}

	private String codeIndexKey(String studentCode) {
		String safe = studentCode.trim().toUpperCase().replace(':', '_');
		RedisKeyFactory f = redisKeyFactory.getIfAvailable();
		return f != null
				? f.key("demo", "student-code-index", safe)
				: "app-platform:demo:student-code-index:" + safe;
	}
}
