package com.github.karixdev.domainmodelmapperservice.infrastructure.kafka;

import com.github.karixdev.domainmodelmapperservice.application.event.ProcessedRawScheduleEvent;
import com.github.karixdev.domainmodelmapperservice.application.event.RawScheduleEvent;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaConfig {

	@Bean
	ProducerFactory<String, ProcessedRawScheduleEvent> processedRawScheduleEventProducerFactory(
			KafkaProperties properties,
			MeterRegistry meterRegistry
	) {
		ProducerFactory<String, ProcessedRawScheduleEvent> factory =
				new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
		factory.addListener(new MicrometerProducerListener<>(meterRegistry));

		return factory;
	}

	@Bean
	KafkaTemplate<String, ProcessedRawScheduleEvent> processedRawScheduleEventKafkaTemplate(
			ProducerFactory<String, ProcessedRawScheduleEvent> factory,
			@Value("${kafka.observation.producer.enabled}") Boolean isObservationEnabled
	) {
		KafkaTemplate<String, ProcessedRawScheduleEvent> kafkaTemplate = new KafkaTemplate<>(factory);
		kafkaTemplate.setObservationEnabled(isObservationEnabled);

		return kafkaTemplate;
	}

	@Bean
	ConsumerFactory<String, RawScheduleEvent> rawScheduleEventConsumerFactory(
			KafkaProperties properties,
			MeterRegistry meterRegistry
	) {
		ConsumerFactory<String, RawScheduleEvent> factory = new DefaultKafkaConsumerFactory<>(
				properties.buildConsumerProperties(),
				new StringDeserializer(),
				new JsonDeserializer<>(RawScheduleEvent.class, false)
		);
		factory.addListener(new MicrometerConsumerListener<>(meterRegistry));

		return factory;
	}

	@Bean
	ConcurrentKafkaListenerContainerFactory<String, RawScheduleEvent> rawScheduleEventConcurrentKafkaListenerContainerFactory(
			ConsumerFactory<String, RawScheduleEvent> consumerFactory,
			DefaultErrorHandler errorHandler,
			@Value("${kafka.observation.consumer.enabled}") Boolean isObservationEnabled
	) {
		ConcurrentKafkaListenerContainerFactory<String, RawScheduleEvent> factory =
				new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(errorHandler);
		factory.getContainerProperties().setObservationEnabled(isObservationEnabled);

		return factory;
	}

	@Bean
	ProducerFactory<String, RawScheduleEvent> rawScheduleEventProducerFactory(
			KafkaProperties properties,
			MeterRegistry meterRegistry
	) {
		ProducerFactory<String, RawScheduleEvent> factory =
				new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
		factory.addListener(new MicrometerProducerListener<>(meterRegistry));

		return factory;
	}

	@Bean
	KafkaTemplate<String, RawScheduleEvent> rawScheduleEventKafkaTemplate(
			ProducerFactory<String, RawScheduleEvent> producerFactory,
			@Value("${kafka.observation.consumer.enabled}") Boolean isObservationEnabled
	) {
		KafkaTemplate<String, RawScheduleEvent> kafkaTemplate = new KafkaTemplate<>(producerFactory);
		kafkaTemplate.setObservationEnabled(isObservationEnabled);

		return kafkaTemplate;
	}

	@Bean
	DefaultErrorHandler rawScheduleEventErrorHandler(
			@Value("${kafka.topics.dlt}") String dlt,
			@Value("${kafka.config.back-off.interval}") long interval,
			@Value("${kafka.config.back-off.max-attempts}") long maxAttempts,
			KafkaTemplate<String, RawScheduleEvent> kafkaTemplate
	) {
		DeadLetterPublishingRecoverer recover =
				new DeadLetterPublishingRecoverer(kafkaTemplate, (cr, e) -> new TopicPartition(dlt, cr.partition()));

		FixedBackOff bo = new FixedBackOff(interval, maxAttempts);
		DefaultErrorHandler handler = new DefaultErrorHandler(recover, bo);

		handler.setRetryListeners((cr, e, v) -> log.info("Consuming attempt {} after exception: {}", v, e.getClass().getName()));

		return handler;
	}

}
