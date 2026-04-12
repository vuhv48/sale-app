package com.klb.app.application.service.impl.student;

import com.klb.app.application.service.student.ImportedStudentRef;
import com.klb.app.application.service.student.StudentImportService;
import com.klb.app.domain.student.StudentCode;
import com.klb.app.persistence.entity.Student;
import com.klb.app.persistence.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentImportServiceImpl implements StudentImportService {

	private final StudentRepository studentRepository;

	@Override
	@Transactional
	public Optional<ImportedStudentRef> importIfAbsent(String studentCode, String fullName) {
		Optional<StudentCode> codeOpt = StudentCode.tryParseForImport(studentCode);
		if (codeOpt.isEmpty() || studentRepository.existsByStudentCode(codeOpt.get().value())) {
			return Optional.empty();
		}
		StudentCode code = codeOpt.get();
		Student s = new Student();
		s.setStudentCode(code.value());
		s.setFullName(fullName.trim());
		studentRepository.save(s);
		return Optional.of(new ImportedStudentRef(s.getId(), s.getStudentCode(), s.getFullName(), s.getCreatedAt()));
	}
}
