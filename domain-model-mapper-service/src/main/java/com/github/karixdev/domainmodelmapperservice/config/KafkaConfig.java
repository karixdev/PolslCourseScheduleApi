package com.github.karixdev.domainmodelmapperservice.config;

import com.github.karixdev.commonservice.event.schedule.ScheduleDomain;
import com.github.karixdev.commonservice.event.schedule.ScheduleRaw;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaConfig {

	@Bean
	ProducerFactory<String, ScheduleDomain> scheduleDomainProducerFactory(KafkaProperties properties) {
		return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
	}

	@Bean
	KafkaTemplate<String, ScheduleDomain> scheduleDomainKafkaTemplate(
			ProducerFactory<String, ScheduleDomain> factory,
			@Value("${kafka.observation.producer.enabled}") Boolean isObservationEnabled
	) {
		KafkaTemplate<String, ScheduleDomain> kafkaTemplate = new KafkaTemplate<>(factory);
		kafkaTemplate.setObservationEnabled(isObservationEnabled);

		return kafkaTemplate;
	}

	@Bean
	ConsumerFactory<String, ScheduleRaw> scheduleRawConsumerFactory(KafkaProperties properties) {
		return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
	}

	@Bean
	ConcurrentKafkaListenerContainerFactory<String, ScheduleRaw> scheduleRawConcurrentKafkaListenerContainerFactory(
			ConsumerFactory<String, ScheduleRaw> consumerFactory,
			DefaultErrorHandler errorHandler,
			@Value("${kafka.observation.consumer.enabled}") Boolean isObservationEnabled
	) {
		ConcurrentKafkaListenerContainerFactory<String, ScheduleRaw> factory =
				new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(errorHandler);
		factory.getContainerProperties().setObservationEnabled(isObservationEnabled);

		return factory;
	}

	@Bean
	ProducerFactory<String, ScheduleRaw> scheduleRawProducerFactory(KafkaProperties properties) {
		return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
	}

	@Bean
	KafkaTemplate<String, ScheduleRaw> scheduleRawKafkaTemplate(
			ProducerFactory<String, ScheduleRaw> producerFactory,
			@Value("${kafka.observation.consumer.enabled}") Boolean isObservationEnabled
	) {
		KafkaTemplate<String, ScheduleRaw> kafkaTemplate = new KafkaTemplate<>(producerFactory);
		kafkaTemplate.setObservationEnabled(isObservationEnabled);

		return kafkaTemplate;
	}

	@Bean
	DefaultErrorHandler scheduleRawErrorHandler(
			@Value("${kafka.topics.dlt}") String dlt,
			@Value("${kafka.config.back-off.interval}") long interval,
			@Value("${kafka.config.back-off.max-attempts}") long maxAttempts,
			KafkaTemplate<String, ScheduleRaw> kafkaTemplate
	) {
		DeadLetterPublishingRecoverer recover =
				new DeadLetterPublishingRecoverer(kafkaTemplate, (cr, e) -> new TopicPartition(dlt, cr.partition()));

		FixedBackOff bo = new FixedBackOff(interval, maxAttempts);
		DefaultErrorHandler handler = new DefaultErrorHandler(recover, bo);

		handler.setRetryListeners((cr, e, v) -> log.info("Consuming attempt {} after exception: {}", v, e.getClass().getName()));

		return handler;
	}

}
