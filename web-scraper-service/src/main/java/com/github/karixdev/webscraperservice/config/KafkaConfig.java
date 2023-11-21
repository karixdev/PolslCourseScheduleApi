package com.github.karixdev.webscraperservice.config;

import com.github.karixdev.commonservice.event.schedule.ScheduleRaw;
import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import com.github.karixdev.webscraperservice.exception.PlanPolslUnavailableException;
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
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaConfig {

    @Bean
    ConsumerFactory<String, ScheduleEvent> scheduleEventConsumerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, ScheduleEvent> scheduleEventConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<String, ScheduleEvent> consumerFactory,
            DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ScheduleEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    @Bean
    ProducerFactory<String, ScheduleEvent> scheduleEventProducerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
    }

    @Bean
    KafkaTemplate<String, ScheduleEvent> scheduleEventKafkaTemplate(ProducerFactory<String, ScheduleEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    DefaultErrorHandler scheduleEventErrorHandler(
            @Value("${kafka.topics.dlt}") String topicDLT,
            @Value("${kafka.config.back-off.max-retries}") int maxRetries,
            @Value("${kafka.config.back-off.multiplier}") double multiplier,
            @Value("${kafka.config.back-off.interval.initial}") long initialInterval,
            @Value("${kafka.config.back-off.interval.max}") long maxInterval,
            KafkaTemplate<String, ScheduleEvent> kafkaTemplate
    ) {
        DeadLetterPublishingRecoverer recover =
                new DeadLetterPublishingRecoverer(kafkaTemplate, (cr, e) -> new TopicPartition(topicDLT, cr.partition()));

        DefaultErrorHandler handler = new DefaultErrorHandler(recover);
        handler.setBackOffFunction((cr, ex) -> {
            if (ex instanceof PlanPolslUnavailableException) {
                ExponentialBackOff bo = new ExponentialBackOffWithMaxRetries(maxRetries);
                bo.setInitialInterval(initialInterval);
                bo.setMultiplier(multiplier);
                bo.setMaxInterval(maxInterval);
            }
            return new FixedBackOff(0L, 0L);
        });
        handler.setRetryListeners((cr, e, v) -> log.info("Consuming attempt {} after exception: {}", v, e.getClass().getName()));

        return handler;
    }

    @Bean
    ProducerFactory<String, ScheduleRaw> scheduleRawProducerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
    }

    @Bean
    KafkaTemplate<String, ScheduleRaw> scheduleRawKafkaTemplate(ProducerFactory<String, ScheduleRaw> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

}
