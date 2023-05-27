package com.github.karixdev.scheduleservice.producer;

import com.github.karixdev.scheduleservice.message.ScheduleEventMessage;
import com.github.karixdev.scheduleservice.message.ScheduleEventType;
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

    @Test
    void GivenCreateScheduleEventType_WhenProduceScheduleEventMessage_ThenSendsMessageToCorrectExchangeWithCorrectRoutingKey() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        ScheduleEventType eventType = ScheduleEventType.CREATE;

        ScheduleEventMessage expectedMessage = new ScheduleEventMessage(scheduleId);

        // When
        underTest.produceScheduleEventMessage(scheduleId, eventType);

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
        UUID scheduleId = UUID.randomUUID();
        ScheduleEventType eventType = ScheduleEventType.UPDATE;

        ScheduleEventMessage expectedMessage = new ScheduleEventMessage(scheduleId);

        // When
        underTest.produceScheduleEventMessage(scheduleId, eventType);

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
        UUID scheduleId = UUID.randomUUID();
        ScheduleEventType eventType = ScheduleEventType.DELETE;

        ScheduleEventMessage expectedMessage = new ScheduleEventMessage(scheduleId);

        // When
        underTest.produceScheduleEventMessage(scheduleId, eventType);

        // Then
        verify(rabbitTemplate).convertAndSend(
                SCHEDULE_EXCHANGE,
                SCHEDULE_DELETE_ROUTING_KEY,
                expectedMessage
        );
    }

}