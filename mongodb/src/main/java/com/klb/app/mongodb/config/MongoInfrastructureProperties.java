package com.klb.app.mongodb.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ket noi Mongo — chi khi {@code app.mongodb.enabled=true}.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.mongodb")
public class MongoInfrastructureProperties {

	private String uri = "mongodb://localhost:27017";

	private String database = "sale_app_mongo";
}
