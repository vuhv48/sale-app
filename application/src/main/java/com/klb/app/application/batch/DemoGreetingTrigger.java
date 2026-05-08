package com.klb.app.application.batch;

/**
 * Cổng kích hoạt job demo (triển khai trong module {@code batch}).
 */
public interface DemoGreetingTrigger {

	DemoGreetingJobResult run();
}
