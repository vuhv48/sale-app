package com.klb.app.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "integration_outbox")
public class IntegrationOutbox {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String topic;

	@Column(name = "message_key", nullable = false)
	private String messageKey;

	@Column(nullable = false, columnDefinition = "text")
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private OutboxStatus status = OutboxStatus.PENDING;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "sent_at")
	private Instant sentAt;

	@Column(name = "last_error", length = 2000)
	private String lastError;

	public IntegrationOutbox(String topic, String messageKey, String payload) {
		this.topic = topic;
		this.messageKey = messageKey;
		this.payload = payload;
		this.status = OutboxStatus.PENDING;
	}

	@PrePersist
	void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
