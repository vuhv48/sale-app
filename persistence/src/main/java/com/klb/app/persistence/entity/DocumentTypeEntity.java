package com.klb.app.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "document_types")
public class DocumentTypeEntity extends BaseAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true, length = 100)
	private String code;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(nullable = false)
	private boolean active = true;

	@Column(name = "require_verification", nullable = false)
	private boolean requireVerification = true;

	@Column(name = "max_size_mb")
	private Integer maxSizeMb;

	@Column(name = "allowed_mime_pattern", length = 255)
	private String allowedMimePattern;
}

