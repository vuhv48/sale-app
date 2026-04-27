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
@Table(name = "documents")
public class DocumentEntity extends BaseAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 20)
	private String provider = "minio";

	@Column(nullable = false, length = 255)
	private String bucket;

	@Column(name = "file_path", nullable = false, length = 1024)
	private String filePath;

	@Column(name = "original_filename", length = 512)
	private String originalFilename;

	@Column(name = "mime_type", length = 255)
	private String mimeType;

	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;

	@Column(nullable = false, length = 30)
	private String status = "UPLOADED";
}
