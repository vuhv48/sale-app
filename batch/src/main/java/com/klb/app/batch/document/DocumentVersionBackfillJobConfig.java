package com.klb.app.batch.document;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.UUID;

@Configuration
public class DocumentVersionBackfillJobConfig {

	public static final String JOB_NAME = "documentVersionBackfillJob";

	public static final int DEFAULT_WORKERS = 4;

	@Bean
	public Step documentVersionBackfillStep(
			JobRepository jobRepository,
			PlatformTransactionManager transactionManager,
			DocumentMissingVersionReader reader,
			DocumentVersionBackfillWriter writer,
			TaskExecutor documentBackfillTaskExecutor
	) {
		return new StepBuilder("documentVersionBackfillStep", jobRepository)
				.<UUID, UUID>chunk(200, transactionManager)
				.reader(reader)
				.writer(writer)
				.taskExecutor(documentBackfillTaskExecutor)
				.faultTolerant()
				.retry(TransientDataAccessException.class)
				.retry(CannotAcquireLockException.class)
				.retryLimit(3)
				.skip(DataIntegrityViolationException.class)
				.skip(TransientDataAccessException.class)
				.skip(CannotAcquireLockException.class)
				.skip(IllegalArgumentException.class)
				.skipLimit(100_000)
				.build();
	}

	@Bean
	public TaskExecutor documentBackfillTaskExecutor() {
		SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("doc-backfill-");
		executor.setConcurrencyLimit(DEFAULT_WORKERS);
		return executor;
	}

	@Bean(name = JOB_NAME)
	public Job documentVersionBackfillJob(JobRepository jobRepository, Step documentVersionBackfillStep) {
		return new JobBuilder(JOB_NAME, jobRepository)
				.start(documentVersionBackfillStep)
				.build();
	}
}

