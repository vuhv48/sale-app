package com.klb.app.application.batch;

public record DocumentVersionBackfillResult(
		String exitStatus,
		String status,
		long executionId,
		long readCount,
		long writeCount,
		int chunkSize,
		int maxRounds
) {
}

