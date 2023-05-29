package com.github.karixdev.scheduleservice.producer;

import com.github.karixdev.scheduleservice.entity.Schedule;
import com.github.karixdev.scheduleservice.message.ScheduleEventMessage;
import com.github.karixdev.scheduleservice.message.ScheduleEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static com.github.karixdev.scheduleservice.props.ScheduleMQProperties.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleEventProducerTest {
    @InjectMocks
    ScheduleEventProducer underTest;

    @Mock
    RabbitTemplate rabbitTemplate;

    Schedule exampleSchedule;

    @BeforeEach
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
    }

    @Test
    void GivenCreateScheduleEventType_WhenProduceScheduleEventMessage_ThenSendsMessageToCorrectExchangeWithCorrectRoutingKey() {
        // Given
        Schedule schedule = exampleSchedule;
        ScheduleEventType eventType = ScheduleEventType.CREATE;

        ScheduleEventMessage expectedMessage = new ScheduleEventMessage(
                exampleSchedule.getId(),
                0,
                1,
                4
        );

        // When
        underTest.produceScheduleEventMessage(schedule, eventType);

        // Then
        verify(rabbitTemplate).convertAndSend(
                SCHEDULE_EXCHANGE,
                SCHEDULE_CREATE_ROUTING_KEY,
                expectedMessage
        );
    }

    @Test
    void GivenUpdateScheduleEventType_WhenProduceScheduleEventMessage_ThenSendsMessageToCorrectExchangeWithCorrectRoutingKey() {
        // Given
        Schedule schedule = exampleSchedule;
        ScheduleEventType eventType = ScheduleEventType.UPDATE;

        ScheduleEventMessage expectedMessage = new ScheduleEventMessage(
                schedule.getId(),
                0,
                1,
                4
        );

        // When
        underTest.produceScheduleEventMessage(schedule, eventType);

        // Then
        verify(rabbitTemplate).convertAndSend(
                SCHEDULE_EXCHANGE,
                SCHEDULE_UPDATE_ROUTING_KEY,
                expectedMessage
        );
    }

    @Test
    void GivenDeleteScheduleEventType_WhenProduceScheduleEventMessage_ThenSendsMessageToCorrectExchangeWithCorrectRoutingKey() {
        // Given
        Schedule schedule = exampleSchedule;
        ScheduleEventType eventType = ScheduleEventType.DELETE;

        ScheduleEventMessage expectedMessage = new ScheduleEventMessage(
                schedule.getId(),
                0,
                1,
                4
        );

        // When
        underTest.produceScheduleEventMessage(schedule, eventType);

        // Then
        verify(rabbitTemplate).convertAndSend(
                SCHEDULE_EXCHANGE,
                SCHEDULE_DELETE_ROUTING_KEY,
                expectedMessage
        );
    }

}