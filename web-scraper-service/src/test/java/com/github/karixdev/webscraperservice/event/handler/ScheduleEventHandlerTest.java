package com.github.karixdev.webscraperservice.event.handler;

import com.github.karixdev.commonservice.event.EventType;
import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import com.github.karixdev.commonservice.event.schedule.ScheduleRaw;
import com.github.karixdev.commonservice.model.course.raw.CourseCell;
import com.github.karixdev.commonservice.model.schedule.raw.TimeCell;
import com.github.karixdev.webscraperservice.event.producer.EventProducer;
import com.github.karixdev.webscraperservice.model.PlanPolslResponse;
import com.github.karixdev.webscraperservice.service.PlanPolslService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleEventHandlerTest {

    @InjectMocks
    ScheduleEventHandler underTest;

    @Mock
    PlanPolslService planPolslService;

    @Mock
    EventProducer<ScheduleRaw> producer;

    @Test
    void GivenScheduleEventWithNotSupportedEventType_WhenHandle_ThenEventIsNotProcessed() {
        // Given
        ScheduleEvent event = ScheduleEvent.builder()
                .scheduleId(UUID.randomUUID().toString())
                .eventType(EventType.DELETE)
                .build();

        // When
        underTest.handle(event);

        // Then
        verify(planPolslService, never()).getSchedule(anyInt(), anyInt(), anyInt());
        verify(producer, never()).produce(any());
    }

    @ParameterizedTest
    @MethodSource("supportedEventTypes")
    void GivenScheduleEventWithSupportedEventType_WhenHandle_ThenProducesScheduleRawCourses(EventType eventType) {
        // Given
        ScheduleEvent event = ScheduleEvent.builder()
                .eventType(eventType)
                .scheduleId(UUID.randomUUID().toString())
                .type(0)
                .planPolslId(1337)
                .wd(4)
                .build();

        Set<CourseCell> courseCells = Set.of(CourseCell.builder()
                .top(10)
                .left(20)
                .ch(30)
                .cw(40)
                .text("text")
                .build());
        Set<TimeCell> timeCells = Set.of(new TimeCell("08:30-10:00"));

        when(planPolslService.getSchedule(
                event.planPolslId(),
                event.type(),
                event.wd()
        )).thenReturn(new PlanPolslResponse(timeCells, courseCells));

        // When
        underTest.handle(event);

        // Then
        ScheduleRaw eventToBeProduced = ScheduleRaw.builder()
                .scheduleId(event.scheduleId())
                .courseCells(courseCells)
                .timeCells(timeCells)
                .build();

        verify(producer).produce(eventToBeProduced);
    }

    private static Stream<Arguments> supportedEventTypes() {
        return Stream.of(
                Arguments.of(EventType.CREATE),
                Arguments.of(EventType.UPDATE)
        );
    }

}