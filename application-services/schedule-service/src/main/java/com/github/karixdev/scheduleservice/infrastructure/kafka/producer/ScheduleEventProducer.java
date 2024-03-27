package com.github.karixdev.scheduleservice.infrastructure.kafka.producer;

import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.application.event.producer.EventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

@RequiredArgsConstructor
public class ScheduleEventProducer implements EventProducer<ScheduleEvent> {

    private final KafkaTemplate<String, ScheduleEvent> kafkaTemplate;
    private final String topic;

    @Override
    public void produce(ScheduleEvent event) {
        kafkaTemplate.send(topic, event.scheduleId(), event);
    }

}
