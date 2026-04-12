package com.klb.app.batch.student;

import com.klb.app.application.batch.StudentCsvImportResult;
import com.klb.app.application.batch.StudentCsvImportTrigger;
import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentCsvImportTriggerImpl implements StudentCsvImportTrigger {

	private final StudentImportJobRunner studentImportJobRunner;

	@Override
	public StudentCsvImportResult run() {
		try {
			var execution = studentImportJobRunner.runStudentCsvImport();
			return new StudentCsvImportResult(
					execution.getExitStatus().getExitCode(),
					execution.getStatus().name(),
					execution.getId());
		} catch (Exception e) {
			throw new DomainException(ErrorStatus.BATCH_JOB_FAILED, e.getMessage() != null ? e.getMessage() : ErrorStatus.BATCH_JOB_FAILED.defaultMessage());
		}
	}
}
