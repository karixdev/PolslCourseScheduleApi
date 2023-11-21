package com.github.karixdev.domaincoursemapperservice.config;

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
    KafkaTemplate<String, ScheduleDomain> scheduleDomainKafkaTemplate (ProducerFactory<String, ScheduleDomain> factory) {
        return new KafkaTemplate<>(factory);
    }

    @Bean
    ConsumerFactory<String, ScheduleRaw> scheduleRawConsumerFactory(KafkaProperties properties) {
        return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, ScheduleRaw> scheduleRawConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<String, ScheduleRaw> consumerFactory,
            DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ScheduleRaw> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    @Bean
    ProducerFactory<String, ScheduleRaw> scheduleRawProducerFactory(KafkaProperties properties) {
        return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
    }

    @Bean
    KafkaTemplate<String, ScheduleRaw> scheduleRawKafkaTemplate(ProducerFactory<String, ScheduleRaw> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
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
