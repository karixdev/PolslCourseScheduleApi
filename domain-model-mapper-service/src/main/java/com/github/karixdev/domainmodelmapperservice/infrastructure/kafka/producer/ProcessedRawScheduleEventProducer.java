package com.github.karixdev.domainmodelmapperservice.infrastructure.kafka.producer;

import com.github.karixdev.domainmodelmapperservice.application.event.ProcessedRawScheduleEvent;
import com.github.karixdev.domainmodelmapperservice.application.event.producer.EventProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProcessedRawScheduleEventProducer implements EventProducer<ProcessedRawScheduleEvent> {

    private final String topic;
    private final KafkaTemplate<String, ProcessedRawScheduleEvent> kafkaTemplate;

    public ProcessedRawScheduleEventProducer(
            @Value("${kafka.topics.processed-raw-schedule}") String topic,
            KafkaTemplate<String, ProcessedRawScheduleEvent> kafkaTemplate
    ) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void produce(ProcessedRawScheduleEvent event) {
        kafkaTemplate.send(topic, event.scheduleId(), event);
    }
}
