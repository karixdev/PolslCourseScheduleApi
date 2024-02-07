package com.github.karixdev.scheduleservice.config;

import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.MicrometerProducerListener;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

	@Bean
	ProducerFactory<String, ScheduleEvent> scheduleEventProducerFactory(
			KafkaProperties properties,
			MeterRegistry meterRegistry
	) {
		ProducerFactory<String, ScheduleEvent> factory =
				new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
		factory.addListener(new MicrometerProducerListener<>(meterRegistry));

		return factory;
	}

	@Bean
	KafkaTemplate<String, ScheduleEvent> scheduleEventKafkaTemplate(
			ProducerFactory<String, ScheduleEvent> producerFactory,
			@Value("${kafka.observation.producer.enabled}") Boolean isObservationEnabled
	) {
		KafkaTemplate<String, ScheduleEvent> kafkaTemplate = new KafkaTemplate<>(producerFactory);
		kafkaTemplate.setObservationEnabled(isObservationEnabled);
		return kafkaTemplate;
	}

}
