package com.klb.app.web.controller;

import com.klb.app.web.storage.MinioStorageProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Demo upload file len MinIO (S3-compatible). Luon dang ky route de tranh roi vao static resource handler.
 */
@RestController
@RequestMapping("/api/demo/minio")
public class MinioDemoController {

	private final ObjectProvider<S3Client> s3Client;
	private final ObjectProvider<S3Presigner> s3Presigner;
	private final MinioStorageProperties props;

	public MinioDemoController(
			ObjectProvider<S3Client> s3Client,
			ObjectProvider<S3Presigner> s3Presigner,
			MinioStorageProperties props
	) {
		this.s3Client = s3Client;
		this.s3Presigner = s3Presigner;
		this.props = props;
	}

	private ResponseEntity<Map<String, Object>> minioDisabled() {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
				"ok", false,
				"error", "MinIO storage is disabled or not configured",
				"hint", "Set app.storage.minio.enabled=true and credentials; profile local enables this in application-local.yaml"
		));
	}

	private void ensureBucket(S3Client client) {
		try {
			client.headBucket(HeadBucketRequest.builder().bucket(props.bucket()).build());
		} catch (NoSuchBucketException e) {
			if (!props.autoCreateBucket()) {
				throw e;
			}
			client.createBucket(CreateBucketRequest.builder().bucket(props.bucket()).build());
		}
	}

	@GetMapping("/health")
	public ResponseEntity<Map<String, Object>> health() {
		S3Client client = s3Client.getIfAvailable();
		if (!props.enabled() || client == null) {
			return minioDisabled();
		}
		try {
			ensureBucket(client);
		} catch (NoSuchBucketException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
					"ok", false,
					"error", "Bucket does not exist on MinIO",
					"bucket", props.bucket(),
					"hint", "Create bucket in MinIO console, or set app.storage.minio.auto-create-bucket=true (local dev)"
			));
		}
		return ResponseEntity.ok(Map.of(
				"ok", true,
				"bucket", props.bucket(),
				"endpoint", props.endpoint(),
				"checkedAt", Instant.now().toString()
		));
	}

	/**
	 * Preview/download tam thoi bang presigned URL (redirect). Trinh duyet tu mo duoc neu content-type hop le (anh/pdf...).
	 */
	@GetMapping("/preview")
	public Object preview(@RequestParam("key") String key) {
		S3Presigner presigner = s3Presigner.getIfAvailable();
		if (!props.enabled() || presigner == null) {
			return minioDisabled();
		}
		if (key == null || key.isBlank()) {
			throw new IllegalArgumentException("key is required");
		}
		GetObjectRequest get = GetObjectRequest.builder()
				.bucket(props.bucket())
				.key(key)
				.build();
		var presignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(10))
				.getObjectRequest(get)
				.build();
		var url = presigner.presignGetObject(presignRequest).url().toString();
		return new RedirectView(url);
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Map<String, Object>> upload(@RequestPart("file") MultipartFile file) throws IOException {
		S3Client client = s3Client.getIfAvailable();
		if (!props.enabled() || client == null) {
			return minioDisabled();
		}
		ensureBucket(client);
		if (file.isEmpty()) {
			throw new IllegalArgumentException("file is empty");
		}
		String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin";
		String safeKey = Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + "-" + original;
		PutObjectRequest put = PutObjectRequest.builder()
				.bucket(props.bucket())
				.key(safeKey)
				.contentType(file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE)
				.build();
		client.putObject(put, RequestBody.fromBytes(file.getBytes()));
		return ResponseEntity.ok(Map.of(
				"bucket", props.bucket(),
				"key", safeKey,
				"size", file.getSize(),
				"contentType", file.getContentType() != null ? file.getContentType() : ""
		));
	}
}
