package com.klb.app.kafka.config;

import org.springframework.kafka.core.ConsumerFactory;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Sau {@link FixedBackOff} het luot retry, {@link DeadLetterPublishingRecoverer} gui ban ghi sang topic {@code <topic-goc>.DLT}.
 * Can {@link org.springframework.kafka.annotation.KafkaListener} dung {@link Acknowledgment}: neu throw truoc khi ack thi error handler chay.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class KafkaListenerDlqConfiguration {

	private static final long BACKOFF_MS = 400L;
	/** So lan goi listener toi da (lan dau + retry). */
	private static final long MAX_ATTEMPTS = 3;

	@Bean
	public DefaultErrorHandler kafkaDefaultErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
				kafkaTemplate,
				(record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
		return new DefaultErrorHandler(recoverer, new FixedBackOff(BACKOFF_MS, MAX_ATTEMPTS));
	}

	@Bean
	@Primary
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
			ConsumerFactory<String, String> consumerFactory,
			DefaultErrorHandler kafkaDefaultErrorHandler) {
		ConcurrentKafkaListenerContainerFactory<String, String> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(kafkaDefaultErrorHandler);
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
		return factory;
	}
}
