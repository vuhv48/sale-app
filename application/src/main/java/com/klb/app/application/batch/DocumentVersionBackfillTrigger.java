package com.klb.app.application.batch;

public interface DocumentVersionBackfillTrigger {

	DocumentVersionBackfillResult run(int chunkSize, int maxRounds);
}

