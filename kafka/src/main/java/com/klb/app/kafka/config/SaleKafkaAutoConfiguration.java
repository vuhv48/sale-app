package com.klb.app.kafka.config;

import com.klb.app.common.messaging.DomainEventPublisher;
import com.klb.app.kafka.integration.KafkaDomainEventPublisher;
import com.klb.app.kafka.support.KafkaTopicFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.metrics.KafkaMetricsAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Chi khi {@code app.kafka.enabled=true}: nap {@link KafkaAutoConfiguration} + metrics.
 * Trong YAML can {@code spring.autoconfigure.exclude} hai lop Kafka mac dinh de khi tat khong tao bean.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(KafkaInfrastructureProperties.class)
@Import({
	KafkaAutoConfiguration.class,
	KafkaMetricsAutoConfiguration.class,
	KafkaListenerDlqConfiguration.class
})
public class SaleKafkaAutoConfiguration {

	@Bean
	public KafkaTopicFactory kafkaTopicFactory(KafkaInfrastructureProperties properties) {
		return new KafkaTopicFactory(properties);
	}

	@Bean
	public DomainEventPublisher domainEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
		return new KafkaDomainEventPublisher(kafkaTemplate);
	}
}
