package com.github.karixdev.courseservice.config;

import com.github.karixdev.commonservice.event.course.ScheduleCoursesEvent;
import com.github.karixdev.commonservice.event.schedule.ScheduleDomain;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

@Configuration
public class KafkaConfig {

    @Bean
    ProducerFactory<String, ScheduleDomain> scheduleDomainProducerFactory(
            KafkaProperties properties
    ) {
        return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
    }

    @Bean
    KafkaTemplate<String, ScheduleDomain> scheduleDomainKafkaTemplate(
            ProducerFactory<String, ScheduleDomain> factory
    ) {
        return new KafkaTemplate<>(factory);
    }

    @Bean
    ConsumerFactory<String, ScheduleDomain> scheduleDomainConsumerFactory(
            KafkaProperties properties
    ) {
        return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, ScheduleDomain> stringScheduleDomainConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<String, ScheduleDomain> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ScheduleDomain> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        return factory;
    }

    @Bean
    ProducerFactory<String, ScheduleCoursesEvent> stringScheduleCoursesEventProducerFactory(
            KafkaProperties properties
    ) {
        return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
    }

    @Bean
    KafkaTemplate<String, ScheduleCoursesEvent> scheduleCoursesEventKafkaTemplate(
            ProducerFactory<String, ScheduleCoursesEvent> factory
    ) {
        return new KafkaTemplate<>(factory);
    }

}
