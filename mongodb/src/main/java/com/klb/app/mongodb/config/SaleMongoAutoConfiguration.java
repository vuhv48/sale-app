package com.klb.app.mongodb.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.UuidRepresentation;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Chi khi {@code app.mongodb.enabled=true}. Khong dung spring-boot-starter-data-mongodb
 * de tranh tu dong ket noi khi Mongo tat.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "app.mongodb", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(MongoInfrastructureProperties.class)
@EnableMongoRepositories(basePackageClasses = com.klb.app.mongodb.repository.StudentNoteMongoRepository.class)
public class SaleMongoAutoConfiguration {

	@Bean
	public MongoClient mongoClient(MongoInfrastructureProperties props) {
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString(props.getUri()))
				.uuidRepresentation(UuidRepresentation.STANDARD)
				.build();
		return MongoClients.create(settings);
	}

	@Bean
	public MongoTemplate mongoTemplate(MongoClient mongoClient, MongoInfrastructureProperties props) {
		return new MongoTemplate(mongoClient, props.getDatabase());
	}
}
