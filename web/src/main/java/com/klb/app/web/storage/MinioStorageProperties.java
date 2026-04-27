package com.klb.app.web.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.minio")
public record MinioStorageProperties(
		boolean enabled,
		String endpoint,
		String region,
		String accessKey,
		String secretKey,
		String bucket,
		/** Dev convenience: create bucket if missing (MinIO local). Production should create bucket out-of-band. */
		boolean autoCreateBucket
) {
}
