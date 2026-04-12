package com.klb.app.application.student;

import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import com.klb.app.domain.student.StudentCode;
import com.klb.app.persistence.entity.Student;
import com.klb.app.persistence.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {

	private final StudentRepository studentRepository;

	@Transactional(readOnly = true)
	public List<StudentResponse> listAll() {
		return studentRepository.findAllByOrderByStudentCodeAsc().stream()
				.map(StudentService::toResponse)
				.toList();
	}

	@Transactional
	public StudentResponse create(String studentCode, String fullName) {
		StudentCode code = StudentCode.parse(studentCode);
		if (studentRepository.existsByStudentCode(code.value())) {
			throw new DomainException(ErrorStatus.STUDENT_DUPLICATE_CODE, "Student code already exists: " + code.value());
		}
		Student s = new Student();
		s.setStudentCode(code.value());
		s.setFullName(fullName.trim());
		studentRepository.save(s);
		return toResponse(s);
	}

	/**
	 * Dùng cho batch: tạo nếu chưa có, trùng mã thì bỏ qua dòng (processor trả {@code null}).
	 */
	@Transactional
	public Optional<Student> tryImportStudent(String studentCode, String fullName) {
		Optional<StudentCode> codeOpt = StudentCode.tryParseForImport(studentCode);
		if (codeOpt.isEmpty() || studentRepository.existsByStudentCode(codeOpt.get().value())) {
			return Optional.empty();
		}
		StudentCode code = codeOpt.get();
		Student s = new Student();
		s.setStudentCode(code.value());
		s.setFullName(fullName.trim());
		studentRepository.save(s);
		return Optional.of(s);
	}

	private static StudentResponse toResponse(Student s) {
		return new StudentResponse(s.getId(), s.getStudentCode(), s.getFullName(), s.getCreatedAt());
	}
}
