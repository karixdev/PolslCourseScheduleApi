package com.github.karixdev.webscraperservice.service;

import com.github.karixdev.commonservice.event.EventType;
import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import com.github.karixdev.commonservice.model.course.raw.CourseCell;
import com.github.karixdev.commonservice.model.schedule.raw.TimeCell;
import com.github.karixdev.webscraperservice.exception.EmptyCourseCellsSetException;
import com.github.karixdev.webscraperservice.exception.EmptyTimeCellSetException;
import com.github.karixdev.webscraperservice.model.PlanPolslResponse;
import com.github.karixdev.webscraperservice.producer.ScheduleRawProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @InjectMocks
    ScheduleService underTest;

    @Mock
    PlanPolslService planPolslService;

    @Mock
    ScheduleRawProducer producer;

    @Test
    void GivenScheduleEventWithNotSupportedEventType_WhenHandleScheduleEvent_ThenEventIsNotProcessed() {
        // Given
        ScheduleEvent event = ScheduleEvent.builder()
                .scheduleId(UUID.randomUUID().toString())
                .eventType(EventType.DELETE)
                .build();
        ConsumerRecord<String, ScheduleEvent> consumerRecord = new ConsumerRecord<>("topic", 0, 0, event.scheduleId(), event);

        // When
        underTest.handleScheduleEvent(consumerRecord);

        // Then
        verify(planPolslService, never()).getSchedule(anyInt(), anyInt(), anyInt());
        verify(producer, never()).produceRawCourse(any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("supportedEventTypes")
    void GivenScheduleEventThatResultsWithEmptyCourseCellsSet_WhenHandleScheduleCreateAndUpdate_ThenThrowsEmptyCourseCellsSetException(EventType eventType) {
        // Given
        ScheduleEvent event = ScheduleEvent.builder()
                .eventType(eventType)
                .scheduleId(UUID.randomUUID().toString())
                .type(0)
                .planPolslId(1337)
                .wd(4)
                .build();
        ConsumerRecord<String, ScheduleEvent> consumerRecord = new ConsumerRecord<>("topic", 0, 0, event.scheduleId(), event);

        when(planPolslService.getSchedule(
                event.planPolslId(),
                event.type(),
                event.wd()
        )).thenReturn(new PlanPolslResponse(Set.of(), Set.of()));

        // When & Then
        assertThatThrownBy(() -> underTest.handleScheduleEvent(consumerRecord))
                .isInstanceOf(EmptyCourseCellsSetException.class);
    }

    @ParameterizedTest
    @MethodSource("supportedEventTypes")
    void GivenScheduleEventThatResultsWithEmptyTimeCellsSet_WhenHandleScheduleCreateAndUpdate_ThenThrowsEmptyTimeCellSetException(EventType eventType) {
        // Given
        ScheduleEvent event = ScheduleEvent.builder()
                .eventType(eventType)
                .scheduleId(UUID.randomUUID().toString())
                .type(0)
                .planPolslId(1337)
                .wd(4)
                .build();
        ConsumerRecord<String, ScheduleEvent> consumerRecord = new ConsumerRecord<>("topic", 0, 0, event.scheduleId(), event);

        CourseCell courseCell = CourseCell.builder().build();

        when(planPolslService.getSchedule(
                event.planPolslId(),
                event.type(),
                event.wd()
        )).thenReturn(new PlanPolslResponse(Set.of(), Set.of(courseCell)));

        // When & Then
        assertThatThrownBy(() -> underTest.handleScheduleEvent(consumerRecord))
                .isInstanceOf(EmptyTimeCellSetException.class);
    }

    @ParameterizedTest
    @MethodSource("supportedEventTypes")
    void GivenScheduleEvent_WhenHandleScheduleCreateAndUpdate_ThenProducesRawCoursesMessage(EventType eventType) {
        // Given
        ScheduleEvent event = ScheduleEvent.builder()
                .eventType(eventType)
                .scheduleId(UUID.randomUUID().toString())
                .type(0)
                .planPolslId(1337)
                .wd(4)
                .build();
        ConsumerRecord<String, ScheduleEvent> consumerRecord = new ConsumerRecord<>("topic", 0, 0, event.scheduleId(), event);

        CourseCell courseCell = CourseCell.builder()
                .top(10)
                .left(20)
                .ch(30)
                .cw(40)
                .text("text")
                .build();
        TimeCell timeCell = new TimeCell("08:30-10:00");

        when(planPolslService.getSchedule(
                event.planPolslId(),
                event.type(),
                event.wd()
        )).thenReturn(new PlanPolslResponse(
                Set.of(timeCell),
                Set.of(courseCell)
        ));

        // When
        underTest.handleScheduleEvent(consumerRecord);

        // Then
        verify(producer).produceRawCourse(
                event.scheduleId(),
                Set.of(courseCell),
                Set.of(timeCell)
        );
    }

    private static Stream<Arguments> supportedEventTypes() {
        return Stream.of(
                Arguments.of(EventType.CREATE),
                Arguments.of(EventType.UPDATE)
        );
    }

}
