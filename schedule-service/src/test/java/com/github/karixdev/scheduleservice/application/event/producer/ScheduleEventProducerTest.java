package com.github.karixdev.scheduleservice.application.event.producer;

import com.github.karixdev.commonservice.event.EventType;
import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import com.github.karixdev.scheduleservice.application.event.producer.ScheduleEventProducer;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.mockito.Mockito.verify;

class ScheduleEventProducerTest {

    ScheduleEventProducer underTest;
    KafkaTemplate<String, ScheduleEvent> kafkaTemplate;
    String topic;

    static Schedule exampleSchedule;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        exampleSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule")
                .wd(4)
                .build();

        kafkaTemplate = (KafkaTemplate<String, ScheduleEvent>) Mockito.mock(KafkaTemplate.class);
        topic = "schedule-event";
        underTest = new ScheduleEventProducer(kafkaTemplate, topic);
    }

    @Test
    void GivenSchedule_WhenProduceScheduleCreateEvent_ThenProducesCorrectEventToProperTopic() {
        // Given
        Schedule schedule = exampleSchedule;
        ScheduleEvent event = createEvent(EventType.CREATE);

        // When
        underTest.produceScheduleCreateEvent(schedule);

        // Then
        verify(kafkaTemplate).send(topic, event.scheduleId(), event);
    }

    @Test
    void GivenSchedule_WhenProduceScheduleUpdateEvent_ThenProducesCorrectEventToProperTopic() {
        // Given
        Schedule schedule = exampleSchedule;
        ScheduleEvent event = createEvent(EventType.UPDATE);

        // When
        underTest.produceScheduleUpdateEvent(schedule);

        // Then
        verify(kafkaTemplate).send(topic, event.scheduleId(), event);
    }

    @Test
    void GivenSchedule_WhenProduceScheduleDeleteEvent_ThenProducesCorrectEventToProperTopic() {
        // Given
        Schedule schedule = exampleSchedule;
        ScheduleEvent event = ScheduleEvent.builder()
                .eventType(EventType.DELETE)
                .scheduleId(schedule.getId().toString())
                .build();

        // When
        underTest.produceScheduleDeleteEvent(schedule);

        // Then
        verify(kafkaTemplate).send(topic, event.scheduleId(), event);
    }

    private static ScheduleEvent createEvent(EventType eventType) {
        return ScheduleEvent.builder()
                .eventType(eventType)
                .scheduleId(exampleSchedule.getId().toString())
                .type(exampleSchedule.getType())
                .planPolslId(exampleSchedule.getPlanPolslId())
                .wd(exampleSchedule.getWd())
                .build();
    }

}