package com.klb.app.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "document_versions")
public class DocumentVersionEntity extends BaseAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "document_id", nullable = false)
	private UUID documentId;

	@Column(name = "version_no", nullable = false)
	private int versionNo;

	@Column(name = "storage_provider", nullable = false, length = 20)
	private String storageProvider = "minio";

	@Column(nullable = false, length = 255)
	private String bucket;

	@Column(name = "file_path", nullable = false, length = 1024)
	private String filePath;

	@Column(name = "object_version", length = 255)
	private String objectVersion;

	@Column(length = 128)
	private String etag;

	@Column(name = "original_filename", length = 512)
	private String originalFilename;

	@Column(name = "mime_type", length = 255)
	private String mimeType;

	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;

	@Column(name = "upload_status", nullable = false, length = 30)
	private String uploadStatus = "UPLOADED";

	@Column(name = "uploaded_at", nullable = false)
	private Instant uploadedAt = Instant.now();
}

