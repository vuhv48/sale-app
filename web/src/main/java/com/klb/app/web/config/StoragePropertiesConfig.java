package com.klb.app.web.config;

import com.klb.app.web.storage.MinioStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioStorageProperties.class)
public class StoragePropertiesConfig {
}
