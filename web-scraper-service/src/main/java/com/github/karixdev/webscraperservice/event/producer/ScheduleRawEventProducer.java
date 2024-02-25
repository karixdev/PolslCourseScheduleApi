package com.github.karixdev.webscraperservice.event.producer;

import com.github.karixdev.commonservice.event.schedule.ScheduleRaw;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ScheduleRawEventProducer implements EventProducer<ScheduleRaw> {

    private final KafkaTemplate<String, ScheduleRaw> kafkaTemplate;
    private final String topic;

    public ScheduleRawEventProducer(
            @Value("${kafka.topics.course-raw}") String topic,
            KafkaTemplate<String, ScheduleRaw> kafkaTemplate
    ) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void produce(ScheduleRaw event) {
        kafkaTemplate.send(topic, event.scheduleId(), event);
    }
}
