package com.github.karixdev.courseservice.config;

import com.github.karixdev.commonservice.event.course.ScheduleCoursesEvent;
import com.github.karixdev.commonservice.event.schedule.ScheduleDomain;
import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.hibernate.exception.JDBCConnectionException;
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
    ConcurrentKafkaListenerContainerFactory<String, ScheduleDomain> scheduleDomainConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<String, ScheduleDomain> consumerFactory,
            KafkaTemplate<String, ScheduleDomain> kafkaTemplate,
            @Value("${kafka.topics.schedule-domain-dlt}") String dlt,
            @Value("${kafka.config.back-off.interval}") Long interval,
            @Value("${kafka.config.back-off.max-attempts}") Long maxAttempts,
            @Value("${kafka.config.back-off.db-interval}") Long dbInterval
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ScheduleDomain> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(defaultErrorHandler(
                kafkaTemplate,
                dlt,
                interval,
                maxAttempts,
                dbInterval
        ));

        return factory;
    }

    @Bean
    ConsumerFactory<String, ScheduleEvent> scheduleEventConsumerFactory(
            KafkaProperties properties
    ) {
        return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, ScheduleEvent> scheduleEventConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<String, ScheduleEvent> consumerFactory,
            KafkaTemplate<String, ScheduleEvent> kafkaTemplate,
            @Value("${kafka.topics.schedule-event-dlt}") String dlt,
            @Value("${kafka.config.back-off.interval}") Long interval,
            @Value("${kafka.config.back-off.max-attempts}") Long maxAttempts,
            @Value("${kafka.config.back-off.db-interval}") Long dbInterval
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ScheduleEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(defaultErrorHandler(
                kafkaTemplate,
                dlt,
                interval,
                maxAttempts,
                dbInterval
        ));

        return factory;
    }

    @Bean
    ProducerFactory<String, ScheduleEvent> scheduleEventProducerFactory(
            KafkaProperties properties
    ) {
        return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
    }

    @Bean
    KafkaTemplate<String, ScheduleEvent> scheduleEventKafkaTemplate(
            ProducerFactory<String, ScheduleEvent> factory
    ) {
        return new KafkaTemplate<>(factory);
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

    <K, V> DefaultErrorHandler defaultErrorHandler(
            KafkaTemplate<K, V> kafkaTemplate,
            String dlt,
            Long backOffInterval,
            Long backOfMaxAttempts,
            Long dbConnectionExceptionBackOffInterval
    ) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate, (cr, e) -> new TopicPartition(dlt, cr.partition()));

        FixedBackOff bo = new FixedBackOff(backOffInterval, backOfMaxAttempts);
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, bo);

        handler.setRetryListeners((cr, e, v) -> log.info("Consuming attempt {} after exception: {}", v, e.getClass().getName()));
        handler.setBackOffFunction((cr, ex) -> {
            if (ex instanceof JDBCConnectionException) {
                return new FixedBackOff(dbConnectionExceptionBackOffInterval, FixedBackOff.UNLIMITED_ATTEMPTS);
            }

            return bo;
        });

        return handler;
    }

}
