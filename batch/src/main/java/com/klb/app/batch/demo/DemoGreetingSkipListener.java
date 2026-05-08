package com.klb.app.batch.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

/**
 * Ghi log khi Spring Batch bỏ qua bản ghi lỗi (skip) — minh họa {@link SkipListener}.
 */
@Slf4j
@Component
public class DemoGreetingSkipListener implements SkipListener<DemoGreetingLine, String> {

	@Override
	public void onSkipInRead(Throwable t) {
		log.warn("[demo-greeting] skip read: {}", t.toString());
	}

	@Override
	public void onSkipInProcess(DemoGreetingLine item, Throwable t) {
		log.warn("[demo-greeting] skip process: name='{}' — {}", item.name(), t.getMessage());
	}

	@Override
	public void onSkipInWrite(String item, Throwable t) {
		log.warn("[demo-greeting] skip write: item='{}' — {}", item, t.getMessage());
	}
}
