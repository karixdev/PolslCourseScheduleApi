package com.github.karixdev.scheduleservice.config;

import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Bean
    ProducerFactory<String, ScheduleEvent> scheduleEventProducerFactory(
            KafkaProperties properties
    ) {
        return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
    }

    @Bean
    KafkaTemplate<String, ScheduleEvent> scheduleEventKafkaTemplate(
            ProducerFactory<String, ScheduleEvent> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

}
