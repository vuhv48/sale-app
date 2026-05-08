package com.klb.app.batch.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DemoGreetingJobRunner {

	private final JobLauncher jobLauncher;
	@Qualifier(DemoGreetingJobConfig.JOB_NAME)
	private final Job demoGreetingJob;

	public JobExecution run() throws Exception {
		var params = new JobParametersBuilder()
				.addLong("run.id", System.currentTimeMillis())
				.toJobParameters();
		return jobLauncher.run(demoGreetingJob, params);
	}
}
