package com.klb.app.mongodb.repository;

import com.klb.app.mongodb.document.StudentNoteDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface StudentNoteMongoRepository extends MongoRepository<StudentNoteDocument, String> {

	List<StudentNoteDocument> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
}
