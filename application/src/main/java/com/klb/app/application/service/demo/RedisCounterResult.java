package com.klb.app.application.service.demo;

/**
 * Ket qua goi demo INCR tren Redis.
 */
public record RedisCounterResult(boolean redisAvailable, Long value) {
}
