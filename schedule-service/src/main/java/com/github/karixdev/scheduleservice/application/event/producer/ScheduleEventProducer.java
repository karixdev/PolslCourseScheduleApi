package com.github.karixdev.scheduleservice.application.event.producer;

import com.github.karixdev.commonservice.event.EventType;
import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ScheduleEventProducer {

    private final KafkaTemplate<String, ScheduleEvent> kafkaTemplate;
    private final String topic;

    public ScheduleEventProducer(
            KafkaTemplate<String, ScheduleEvent> kafkaTemplate,
            @Value("${kafka.topics.schedule-event}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void produceScheduleCreateEvent(Schedule schedule) {
        ScheduleEvent event = ScheduleEvent.builder()
                .eventType(EventType.CREATE)
                .scheduleId(schedule.getId().toString())
                .type(schedule.getType())
                .planPolslId(schedule.getPlanPolslId())
                .wd(schedule.getWd())
                .build();

        produceScheduleEvent(event);
    }

    public void produceScheduleUpdateEvent(Schedule schedule) {
        ScheduleEvent event = ScheduleEvent.builder()
                .eventType(EventType.UPDATE)
                .scheduleId(schedule.getId().toString())
                .type(schedule.getType())
                .planPolslId(schedule.getPlanPolslId())
                .wd(schedule.getWd())
                .build();

        produceScheduleEvent(event);
    }

    public void produceScheduleDeleteEvent(Schedule schedule) {
        ScheduleEvent event = ScheduleEvent.builder()
                .eventType(EventType.DELETE)
                .scheduleId(schedule.getId().toString())
                .build();

        produceScheduleEvent(event);
    }

    private void produceScheduleEvent(ScheduleEvent scheduleEvent) {
        kafkaTemplate.send(topic, scheduleEvent.scheduleId(), scheduleEvent);
    }

}
