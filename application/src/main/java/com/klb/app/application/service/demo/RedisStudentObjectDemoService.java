package com.klb.app.application.service.demo;

import java.util.UUID;

/**
 * Minh hoa: luu {@link RedisStudentSnippet} JSON theo key id;
 * tim theo ma SV bang key phu <code>code -&gt; id</code>.
 */
public interface RedisStudentObjectDemoService {

	RedisStudentObjectDemoResult save(RedisStudentSnippet snippet);

	RedisStudentObjectDemoResult getById(UUID id);

	RedisStudentObjectDemoResult getByStudentCode(String studentCode);
}
