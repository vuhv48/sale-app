package com.klb.app.application.service.impl.demo;

import com.klb.app.application.service.demo.MultiThreadCounterDemoResult;
import com.klb.app.application.service.demo.MultiThreadCounterDemoService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
public class MultiThreadCounterDemoServiceImpl implements MultiThreadCounterDemoService {

	@Override
	public MultiThreadCounterDemoResult runCounterRaceDemo(int threads, int incrementsPerThread) {
		int safeThreads = Math.max(2, Math.min(threads, 64));
		int safeIncrements = Math.max(1_000, Math.min(incrementsPerThread, 2_000_000));
		long expected = (long) safeThreads * safeIncrements;

		UnsafeCounter unsafeCounter = new UnsafeCounter();
		long unsafeDurationMs = runIncrementScenario(unsafeCounter::increment, safeThreads, safeIncrements);

		SafeCounter safeCounter = new SafeCounter();
		long safeDurationMs = runIncrementScenario(safeCounter::increment, safeThreads, safeIncrements);

		long unsafeActual = unsafeCounter.get();
		long safeActual = safeCounter.get();

		return new MultiThreadCounterDemoResult(
				safeThreads,
				safeIncrements,
				expected,
				unsafeActual,
				safeActual,
				Math.max(0L, expected - unsafeActual),
				unsafeDurationMs,
				safeDurationMs
		);
	}

	private long runIncrementScenario(Runnable incrementAction, int threads, int incrementsPerThread) {
		CountDownLatch ready = new CountDownLatch(threads);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(threads);
		List<Thread> workers = new ArrayList<>(threads);

		for (int i = 0; i < threads; i++) {
			Thread thread = new Thread(() -> {
				ready.countDown();
				awaitLatch(start);
				for (int j = 0; j < incrementsPerThread; j++) {
					incrementAction.run();
				}
				done.countDown();
			}, "demo-counter-" + i);
			workers.add(thread);
			thread.start();
		}

		awaitLatch(ready);
		long startedAt = System.nanoTime();
		start.countDown();
		awaitLatch(done);
		long endedAt = System.nanoTime();

		for (Thread worker : workers) {
			try {
				worker.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Interrupted while joining worker thread", e);
			}
		}

		return (endedAt - startedAt) / 1_000_000;
	}

	private void awaitLatch(CountDownLatch latch) {
		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting for latch", e);
		}
	}

	private static final class UnsafeCounter {
		private long value;

		void increment() {
			value++;
		}

		long get() {
			return value;
		}
	}

	private static final class SafeCounter {
		private long value;

		synchronized void increment() {
			value++;
		}

		synchronized long get() {
			return value;
		}
	}
}
