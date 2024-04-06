package com.github.karixdev.courseservice.infrastructure.kafka.config;

import com.github.karixdev.courseservice.application.event.ProcessedRawScheduleEvent;
import com.github.karixdev.courseservice.application.event.ScheduleEvent;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
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
    ConsumerFactory<String, ScheduleEvent> scheduleEventConsumerFactory(
            KafkaProperties properties,
            MeterRegistry meterRegistry
    ) {
        ConsumerFactory<String, ScheduleEvent> factory = new DefaultKafkaConsumerFactory<>(
                properties.buildConsumerProperties(),
                new StringDeserializer(),
                new JsonDeserializer<>(ScheduleEvent.class, false)
        );
        factory.addListener(new MicrometerConsumerListener<>(meterRegistry));

        return factory;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, ScheduleEvent> scheduleEventConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<String, ScheduleEvent> consumerFactory,
            KafkaProperties properties,
            MeterRegistry meterRegistry,
            @Value("${kafka.topics.schedule-event-dlt}") String dlt,
            @Value("${kafka.config.back-off.interval}") Long interval,
            @Value("${kafka.config.back-off.max-attempts}") Long maxAttempts,
            @Value("${kafka.observation.consumer.enabled}") Boolean isObservationEnabled
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ScheduleEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(defaultErrorHandler(
                properties,
                meterRegistry,
                dlt,
                interval,
                maxAttempts
        ));

        factory.getContainerProperties().setObservationEnabled(isObservationEnabled);

        return factory;
    }

    @Bean
    ConsumerFactory<String, ProcessedRawScheduleEvent> processedRawScheduleEventConsumerFactory(
            KafkaProperties properties,
            MeterRegistry meterRegistry
    ) {
        ConsumerFactory<String, ProcessedRawScheduleEvent> factory = new DefaultKafkaConsumerFactory<>(
                properties.buildConsumerProperties(),
                new StringDeserializer(),
                new JsonDeserializer<>(ProcessedRawScheduleEvent.class, false)
        );
        factory.addListener(new MicrometerConsumerListener<>(meterRegistry));

        return factory;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, ProcessedRawScheduleEvent> processedRawScheduleEventConcurrentKafkaListenerContainerFactory(
            ConsumerFactory<String, ProcessedRawScheduleEvent> consumerFactory,
            KafkaProperties properties,
            MeterRegistry meterRegistry,
            @Value("${kafka.topics.processed-raw-schedule-dlt}") String dlt,
            @Value("${kafka.config.back-off.interval}") Long interval,
            @Value("${kafka.config.back-off.max-attempts}") Long maxAttempts,
            @Value("${kafka.observation.consumer.enabled}") Boolean isObservationEnabled
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ProcessedRawScheduleEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(defaultErrorHandler(
                properties,
                meterRegistry,
                dlt,
                interval,
                maxAttempts
        ));

        factory.getContainerProperties().setObservationEnabled(isObservationEnabled);

        return factory;
    }

    <K, V> KafkaTemplate<K, V> createKafkaTemplate(
            KafkaProperties properties,
            MeterRegistry meterRegistry
    ) {
        ProducerFactory<K, V> factory =
                new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
        factory.addListener(new MicrometerProducerListener<>(meterRegistry));

        KafkaTemplate<K, V> kafkaTemplate = new KafkaTemplate<>(factory);
        kafkaTemplate.setObservationEnabled(true);

        return kafkaTemplate;
    }

    <K, V> DefaultErrorHandler defaultErrorHandler(
            KafkaProperties properties,
            MeterRegistry meterRegistry,
            String dlt,
            Long backOffInterval,
            Long backOfMaxAttempts
    ) {
        KafkaTemplate<K, V> kafkaTemplate = createKafkaTemplate(properties, meterRegistry);

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate, (cr, e) -> new TopicPartition(dlt, cr.partition()));

        FixedBackOff bo = new FixedBackOff(backOffInterval, backOfMaxAttempts);
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, bo);

        handler.setRetryListeners((cr, e, v) -> log.info("Consuming attempt {} after exception: {}", v, e.getClass().getName()));

        return handler;
    }


}
