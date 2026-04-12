package com.klb.app.persistence.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EnableJpaRepositories(basePackages = "com.klb.app.persistence.repository")
@EntityScan(basePackages = "com.klb.app.persistence.entity")
public class PersistenceAutoConfiguration {
}
