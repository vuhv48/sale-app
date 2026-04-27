package com.klb.app.batch.document;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentVersionBackfillJobRunner {

	private final JobLauncher jobLauncher;
	@Qualifier(DocumentVersionBackfillJobConfig.JOB_NAME)
	private final Job documentVersionBackfillJob;

	public JobExecution run(int chunkSize, int maxRounds) throws Exception {
		var parameters = new JobParametersBuilder()
				.addLong("run.id", System.currentTimeMillis())
				.addLong("chunkSize", (long) Math.max(1, chunkSize))
				.addLong("maxRounds", (long) Math.max(1, maxRounds))
				.toJobParameters();
		return jobLauncher.run(documentVersionBackfillJob, parameters);
	}
}

