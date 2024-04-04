package com.github.karixdev.courseservice.infrastructure.kafka.config;

import com.github.karixdev.courseservice.application.event.ProcessedRawScheduleEvent;
import com.github.karixdev.courseservice.application.event.ScheduleEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaConfig {

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
            @Value("${kafka.topics.schedule-domain-dlt}") String dlt,
            @Value("${kafka.config.back-off.interval}") Long interval,
            @Value("${kafka.config.back-off.max-attempts}") Long maxAttempts,
            @Value("${kafka.observation.consumer.enabled}") Boolean isObservationEnabled
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ScheduleEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(defaultErrorHandler(
                kafkaTemplate,
                dlt,
                interval,
                maxAttempts
        ));

        factory.getContainerProperties().setObservationEnabled(isObservationEnabled);

        return factory;
    }

    @Bean
    ConsumerFactory<String, ProcessedRawScheduleEvent> processedRawScheduleEventConsumerFactory(
            KafkaProperties properties
    ) {
        return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, ProcessedRawScheduleEvent> processedRawScheduleEventConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<String, ProcessedRawScheduleEvent> consumerFactory,
            KafkaTemplate<String, ProcessedRawScheduleEvent> kafkaTemplate,
            @Value("${kafka.topics.schedule-domain-dlt}") String dlt,
            @Value("${kafka.config.back-off.interval}") Long interval,
            @Value("${kafka.config.back-off.max-attempts}") Long maxAttempts,
            @Value("${kafka.observation.consumer.enabled}") Boolean isObservationEnabled
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ProcessedRawScheduleEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(defaultErrorHandler(
                kafkaTemplate,
                dlt,
                interval,
                maxAttempts
        ));

        factory.getContainerProperties().setObservationEnabled(isObservationEnabled);

        return factory;
    }

    <K, V> DefaultErrorHandler defaultErrorHandler(
            KafkaTemplate<K, V> kafkaTemplate,
            String dlt,
            Long backOffInterval,
            Long backOfMaxAttempts
    ) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate, (cr, e) -> new TopicPartition(dlt, cr.partition()));

        FixedBackOff bo = new FixedBackOff(backOffInterval, backOfMaxAttempts);
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, bo);

        handler.setRetryListeners((cr, e, v) -> log.info("Consuming attempt {} after exception: {}", v, e.getClass().getName()));

        return handler;
    }


}
