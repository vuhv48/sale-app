package com.klb.app.web.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class MinioS3Config {

	@Bean(destroyMethod = "close")
	@ConditionalOnProperty(prefix = "app.storage.minio", name = "enabled", havingValue = "true")
	S3Presigner minioS3Presigner(MinioStorageProperties props) {
		if (props.accessKey() == null || props.accessKey().isBlank()
				|| props.secretKey() == null || props.secretKey().isBlank()) {
			throw new IllegalStateException("app.storage.minio.access-key/secret-key are required when MinIO is enabled");
		}
		AwsBasicCredentials credentials = AwsBasicCredentials.create(props.accessKey(), props.secretKey());
		S3Configuration s3Configuration = S3Configuration.builder()
				.pathStyleAccessEnabled(true)
				.build();
		return S3Presigner.builder()
				.endpointOverride(URI.create(props.endpoint()))
				.region(Region.of(props.region()))
				.serviceConfiguration(s3Configuration)
				.credentialsProvider(StaticCredentialsProvider.create(credentials))
				.build();
	}

	@Bean
	@ConditionalOnProperty(prefix = "app.storage.minio", name = "enabled", havingValue = "true")
	S3Client minioS3Client(MinioStorageProperties props) {
		if (props.accessKey() == null || props.accessKey().isBlank()
				|| props.secretKey() == null || props.secretKey().isBlank()) {
			throw new IllegalStateException("app.storage.minio.access-key/secret-key are required when MinIO is enabled");
		}
		AwsBasicCredentials credentials = AwsBasicCredentials.create(props.accessKey(), props.secretKey());
		S3Configuration s3Configuration = S3Configuration.builder()
				.pathStyleAccessEnabled(true)
				.build();
		return S3Client.builder()
				.endpointOverride(URI.create(props.endpoint()))
				.region(Region.of(props.region()))
				.serviceConfiguration(s3Configuration)
				.credentialsProvider(StaticCredentialsProvider.create(credentials))
				.build();
	}
}
