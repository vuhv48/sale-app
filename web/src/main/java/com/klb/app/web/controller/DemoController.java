package com.klb.app.web.controller;

import com.klb.app.application.service.demo.KafkaPingDemoService;
import com.klb.app.application.service.demo.KafkaPingResult;
import com.klb.app.application.service.demo.RedisCounterDemoService;
import com.klb.app.application.service.demo.RedisCounterResult;
import com.klb.app.application.service.demo.RedisStudentObjectDemoResult;
import com.klb.app.application.service.demo.RedisStudentObjectDemoService;
import com.klb.app.application.service.demo.MongoStudentNotesAppendResult;
import com.klb.app.application.service.demo.MongoStudentNotesDemoResult;
import com.klb.app.application.service.demo.MongoStudentNotesDemoService;
import com.klb.app.application.service.demo.RedisStudentSnippet;
import com.klb.app.web.dto.MongoStudentNoteAppendRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Example endpoints showing permission-based and role-based checks. Replace with real features later.
 */
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

	private final RedisCounterDemoService redisCounterDemoService;
	private final RedisStudentObjectDemoService redisStudentObjectDemoService;
	private final MongoStudentNotesDemoService mongoStudentNotesDemoService;
	private final KafkaPingDemoService kafkaPingDemoService;

	@GetMapping("/redis-counter")
	public RedisCounterResult redisCounter() {
		return redisCounterDemoService.bumpGlobalDemoCounter();
	}

	/** Chi doc counter trong Redis (khong tang). */
	@GetMapping("/redis-counter/read")
	public RedisCounterResult redisCounterRead() {
		return redisCounterDemoService.getGlobalDemoCounter();
	}

	/** POST JSON {@link RedisStudentSnippet} → luu Redis (JSON + index ma SV). */
	@PostMapping("/redis-student-object")
	public RedisStudentObjectDemoResult redisStudentObjectSave(@RequestBody RedisStudentSnippet body) {
		return redisStudentObjectDemoService.save(body);
	}

	@GetMapping("/redis-student-object/{id}")
	public RedisStudentObjectDemoResult redisStudentObjectById(@PathVariable UUID id) {
		return redisStudentObjectDemoService.getById(id);
	}

	/** Giong tim theo WHERE student_code = ... : doc index roi lay JSON theo id. */
	@GetMapping("/redis-student-object/by-code/{studentCode}")
	public RedisStudentObjectDemoResult redisStudentObjectByCode(@PathVariable String studentCode) {
		return redisStudentObjectDemoService.getByStudentCode(studentCode);
	}

	/** Ghi chu mo rong (Mongo collection {@code student_notes_demo}), {@code studentId} khop UUID Postgres. */
	@PostMapping("/mongo-student-notes")
	public MongoStudentNotesAppendResult mongoStudentNotesAppend(@Valid @RequestBody MongoStudentNoteAppendRequest body) {
		return mongoStudentNotesDemoService.append(body.studentId(), body.body());
	}

	@GetMapping("/mongo-student-notes/{studentId}")
	public MongoStudentNotesDemoResult mongoStudentNotesList(@PathVariable UUID studentId) {
		return mongoStudentNotesDemoService.listByStudent(studentId);
	}

	@GetMapping("/kafka-ping")
	public KafkaPingResult kafkaPing() {
		return kafkaPingDemoService.sendPing();
	}

	/** Producer → topic demo.echo; log consumer #2 trong {@code DemoKafkaListeners}. */
	@GetMapping("/kafka-echo")
	public KafkaPingResult kafkaEcho() {
		return kafkaPingDemoService.sendEcho();
	}

	/** Producer gui payload kich hoat loi listener ping → retry → topic {@code demo.ping.DLT}. */
	@GetMapping("/kafka-ping-dlq")
	public KafkaPingResult kafkaPingDlq() {
		return kafkaPingDemoService.sendPingDlqDemo();
	}

	@GetMapping("/admin")
	public Map<String, String> adminOnly() {
		return Map.of("message", "admin permission required");
	}

	@GetMapping("/user-role")
	public Map<String, String> userRole() {
		return Map.of("message", "ROLE_USER required");
	}
}
