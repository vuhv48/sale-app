package com.klb.app.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties({ JwtProperties.class, SecurityPermitProperties.class })
public class SecurityAutoConfiguration {
}
