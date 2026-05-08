package com.klb.app.batch.demo;

/**
 * Lỗi "tạm thời" minh họa {@code .retry(...)} — không dùng cho dữ liệu sai cố định.
 */
public class DemoGreetingTransientException extends RuntimeException {

	public DemoGreetingTransientException(String message) {
		super(message);
	}
}
