package com.github.karixdev.webscraperservice.consumer;

import com.github.karixdev.commonservice.event.EventType;
import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import com.github.karixdev.webscraperservice.service.ScheduleService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleEventConsumerTest {

    @InjectMocks
    ScheduleEventConsumer underTest;

    @Mock
    ScheduleService service;

    @Test
    void GivenConsumerRecord_WhenConsumeScheduleEvent_ThenEventIsHandled() {
        // Given
        ScheduleEvent event = ScheduleEvent.builder()
                .eventType(EventType.CREATE)
                .scheduleId(UUID.randomUUID().toString())
                .type(0)
                .planPolslId(1337)
                .wd(4)
                .build();
        ConsumerRecord<String, ScheduleEvent> consumerRecord = new ConsumerRecord<>("topic", 0, 0, event.scheduleId(), event);

        // When
        underTest.consumeScheduleEvent(consumerRecord);

        // Then
        verify(service).handleScheduleEvent(consumerRecord);
    }

}