package com.klb.app.batch.student;

import com.klb.app.application.service.student.ImportedStudentRef;
import com.klb.app.application.service.student.StudentImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentImportItemProcessor implements ItemProcessor<StudentCsvLine, ImportedStudentRef> {

	private final StudentImportService studentImportService;

	@Override
	public ImportedStudentRef process(@NonNull StudentCsvLine line) {
		return studentImportService.importIfAbsent(line.studentCode(), line.fullName()).orElse(null);
	}
}
