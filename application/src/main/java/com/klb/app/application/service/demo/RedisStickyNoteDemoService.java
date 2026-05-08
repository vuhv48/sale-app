package com.klb.app.application.service.demo;

public interface RedisStickyNoteDemoService {

	RedisStickyNoteDemoResult get();

	RedisStickyNoteDemoResult save(String text);
}
