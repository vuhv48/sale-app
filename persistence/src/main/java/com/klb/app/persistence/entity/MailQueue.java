package com.klb.app.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "mail_queue")
public class MailQueue {

	@Id
	private UUID id;

	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "to_address", nullable = false, length = 320)
	private String toAddress;

	@Column(name = "template_code", nullable = false, length = 64)
	private String templateCode;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private Map<String, String> variables;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private MailQueueStatus status = MailQueueStatus.PENDING;

	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;

	@Column(name = "last_error", length = 2000)
	private String lastError;

	@Column(name = "sent_at")
	private Instant sentAt;

	@Column(name = "idempotency_key", nullable = false, length = 128, unique = true)
	private String idempotencyKey;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}
}
