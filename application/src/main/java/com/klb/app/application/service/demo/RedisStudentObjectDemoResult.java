package com.klb.app.application.service.demo;

/**
 * Ket qua doc/ghi object demo trong Redis.
 *
 * @param redisAvailable Redis bat va template co san
 * @param student object neu doc duoc (JSON parse OK), null neu miss hoac Redis tat
 */
public record RedisStudentObjectDemoResult(boolean redisAvailable, RedisStudentSnippet student) {
}
