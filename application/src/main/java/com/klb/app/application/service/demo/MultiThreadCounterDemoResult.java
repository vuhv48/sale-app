package com.klb.app.application.service.demo;

public record MultiThreadCounterDemoResult(
		int threads,
		int incrementsPerThread,
		long expected,
		long unsafeActual,
		long safeActual,
		long unsafeLostUpdates,
		long unsafeDurationMs,
		long safeDurationMs
) {
}
