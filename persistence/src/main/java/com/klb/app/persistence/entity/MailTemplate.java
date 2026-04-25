package com.klb.app.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "mail_template")
public class MailTemplate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 64)
	private String code;

	@Column(name = "subject_template", nullable = false, length = 500)
	private String subjectTemplate;

	@Column(name = "body_html", nullable = false, columnDefinition = "text")
	private String bodyHtml;

	@Column(name = "body_text", columnDefinition = "text")
	private String bodyText;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}
}
