package com.github.karixdev.webscraperservice.infrastructure.kafka.producer;

import com.github.karixdev.webscraperservice.application.event.RawScheduleEvent;
import com.github.karixdev.webscraperservice.application.event.producer.EventProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RawScheduleEventProducer implements EventProducer<RawScheduleEvent> {

    private final KafkaTemplate<String, RawScheduleEvent> kafkaTemplate;
    private final String topic;

    public RawScheduleEventProducer(
            @Value("${kafka.topics.course-raw}") String topic,
            KafkaTemplate<String, RawScheduleEvent> kafkaTemplate
    ) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void produce(RawScheduleEvent event) {
        kafkaTemplate.send(topic, event.scheduleId(), event);
    }

}
