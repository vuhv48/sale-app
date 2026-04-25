package com.klb.app.application.service.impl.student;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klb.app.application.service.student.StudentCreatedEvent;
import com.klb.app.application.service.student.StudentPageResponse;
import com.klb.app.application.service.student.StudentResponse;
import com.klb.app.application.service.student.StudentService;
import com.klb.app.common.api.ErrorStatus;
import com.klb.app.common.exception.DomainException;
import com.klb.app.domain.student.StudentCode;
import com.klb.app.kafka.support.KafkaTopicFactory;
import com.klb.app.persistence.entity.Student;
import com.klb.app.persistence.entity.IntegrationOutbox;
import com.klb.app.persistence.repository.IntegrationOutboxRepository;
import com.klb.app.persistence.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

	private static final ObjectMapper OUTBOX_JSON = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	private final StudentRepository studentRepository;
	private final IntegrationOutboxRepository integrationOutboxRepository;
	private final ObjectProvider<KafkaTopicFactory> kafkaTopicFactory;

	@Override
	@Transactional(readOnly = true)
	public StudentPageResponse listPage(Pageable pageable) {
		Page<Student> page = studentRepository.findAllActiveOrderByStudentCodeAsc(pageable);
		return new StudentPageResponse(
				page.getContent().stream().map(StudentServiceImpl::toResponse).toList(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isFirst(),
				page.isLast());
	}

	@Override
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
		enqueueStudentCreatedOutbox(s);
		return toResponse(s);
	}

	private void enqueueStudentCreatedOutbox(Student s) {
		KafkaTopicFactory topics = kafkaTopicFactory.getIfAvailable();
		if (topics == null) {
			return;
		}
		try {
			String topic = topics.topic("student", "created");
			var event = new StudentCreatedEvent(s.getId(), s.getStudentCode(), s.getFullName(), s.getCreatedAt());
			String payload = OUTBOX_JSON.writeValueAsString(event);
			integrationOutboxRepository.save(new IntegrationOutbox(topic, s.getId().toString(), payload));
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Cannot serialize StudentCreatedEvent for outbox", e);
		}
	}

	private static StudentResponse toResponse(Student s) {
		return new StudentResponse(s.getId(), s.getStudentCode(), s.getFullName(), s.getCreatedAt());
	}
}
