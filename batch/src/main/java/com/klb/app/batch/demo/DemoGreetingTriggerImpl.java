package com.klb.app.batch.demo;

import com.klb.app.application.batch.DemoGreetingJobResult;
import com.klb.app.application.batch.DemoGreetingTrigger;
import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DemoGreetingTriggerImpl implements DemoGreetingTrigger {

	private final DemoGreetingJobRunner demoGreetingJobRunner;

	@Override
	public DemoGreetingJobResult run() {
		try {
			JobExecution execution = demoGreetingJobRunner.run();
			JobInstance instance = execution.getJobInstance();
			return new DemoGreetingJobResult(
					instance.getJobName(),
					instance.getInstanceId(),
					execution.getId(),
					execution.getStatus().name(),
					execution.getExitStatus().getExitCode());
		} catch (Exception e) {
			throw new DomainException(ErrorStatus.BATCH_JOB_FAILED, e.getMessage() != null ? e.getMessage() : ErrorStatus.BATCH_JOB_FAILED.defaultMessage());
		}
	}
}
