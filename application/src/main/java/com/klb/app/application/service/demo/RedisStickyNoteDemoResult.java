package com.klb.app.application.service.demo;

/** Ket qua doc/ghi ghi chu demo trong Redis (khong thay cho DB). */
public record RedisStickyNoteDemoResult(boolean redisEnabled, String text) {
}
