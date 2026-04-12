package com.klb.app.batch.student;

import com.klb.app.application.student.StudentService;
import com.klb.app.persistence.entity.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentImportItemProcessor implements ItemProcessor<StudentCsvLine, Student> {

	private final StudentService studentService;

	@Override
	public Student process(@NonNull StudentCsvLine line) {
		return studentService.tryImportStudent(line.studentCode(), line.fullName()).orElse(null);
	}
}
