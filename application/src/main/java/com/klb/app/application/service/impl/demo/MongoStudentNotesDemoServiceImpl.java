package com.klb.app.application.service.impl.demo;

import com.klb.app.application.service.demo.MongoStudentNoteItem;
import com.klb.app.application.service.demo.MongoStudentNotesAppendResult;
import com.klb.app.application.service.demo.MongoStudentNotesDemoResult;
import com.klb.app.application.service.demo.MongoStudentNotesDemoService;
import com.klb.app.mongodb.document.StudentNoteDocument;
import com.klb.app.mongodb.repository.StudentNoteMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MongoStudentNotesDemoServiceImpl implements MongoStudentNotesDemoService {

	private final ObjectProvider<StudentNoteMongoRepository> repository;

	@Override
	public MongoStudentNotesAppendResult append(UUID studentId, String body) {
		StudentNoteMongoRepository repo = repository.getIfAvailable();
		if (repo == null) {
			return MongoStudentNotesAppendResult.unavailable();
		}
		if (body == null || body.isBlank()) {
			throw new IllegalArgumentException("body bat buoc");
		}
		StudentNoteDocument doc = new StudentNoteDocument();
		doc.setId(studentId.toString());
		doc.setStudentId(studentId);
		doc.setBody(body.trim());
		doc.setCreatedAt(Instant.now());
		StudentNoteDocument saved = repo.save(doc);
		return new MongoStudentNotesAppendResult(true, toItem(saved));
	}

	@Override
	public MongoStudentNotesDemoResult listByStudent(UUID studentId) {
		StudentNoteMongoRepository repo = repository.getIfAvailable();
		if (repo == null) {
			return MongoStudentNotesDemoResult.unavailable();
		}
		List<MongoStudentNoteItem> list = repo.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
				.map(MongoStudentNotesDemoServiceImpl::toItem)
				.toList();
		return new MongoStudentNotesDemoResult(true, list);
	}

	private static MongoStudentNoteItem toItem(StudentNoteDocument d) {
		return new MongoStudentNoteItem(d.getId(), d.getStudentId(), d.getBody(), d.getCreatedAt());
	}
}
