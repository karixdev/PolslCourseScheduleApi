package com.github.karixdev.domainmodelmapperservice.application.event.handler;

import com.github.karixdev.domainmodelmapperservice.application.event.ProcessedRawScheduleEvent;
import com.github.karixdev.domainmodelmapperservice.application.event.RawScheduleEvent;
import com.github.karixdev.domainmodelmapperservice.application.event.producer.EventProducer;
import com.github.karixdev.domainmodelmapperservice.application.exception.EmptyProcessedRawCourseSetException;
import com.github.karixdev.domainmodelmapperservice.application.exception.NoScheduleStartTimeException;
import com.github.karixdev.domainmodelmapperservice.application.mapper.ProcessedRawCourseMapper;
import com.github.karixdev.domainmodelmapperservice.application.mapper.ProcessedRawTimeIntervalMapper;
import com.github.karixdev.domainmodelmapperservice.domain.processed.*;
import com.github.karixdev.domainmodelmapperservice.domain.raw.RawCourse;
import com.github.karixdev.domainmodelmapperservice.domain.raw.RawSchedule;
import com.github.karixdev.domainmodelmapperservice.domain.raw.RawTimeInterval;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RawScheduleEventHandlerTest {

    @InjectMocks
    RawScheduleEventHandler underTest;

    @Mock
    ProcessedRawCourseMapper courseMapper;

    @Mock
    ProcessedRawTimeIntervalMapper timeIntervalMapper;

    @Mock
    EventProducer<ProcessedRawScheduleEvent> eventProducer;

    @Test
    void GivenEventWithNoScheduleStartTime_WhenHandle_ThenThrowsNoScheduleStartTimeException() {
        // Given
        String scheduleId = UUID.randomUUID().toString();
        RawSchedule rawSchedule = RawSchedule.builder()
                .courses(Set.of())
                .timeIntervals(Set.of())
                .build();
        RawScheduleEvent event = RawScheduleEvent.builder()
                .scheduleId(scheduleId)
                .entity(rawSchedule)
                .build();

        // When & Then
        assertThatThrownBy(() -> underTest.handle(event))
                .isInstanceOf(NoScheduleStartTimeException.class);
    }

    @Test
    void GivenEventSuchThatProcessedRawCourseSetIsEmpty_WhenHandle_ThenThrowsEmptyProcessedRawCourseSetException() {
        // Given
        String scheduleId = UUID.randomUUID().toString();

        RawTimeInterval rawTimeInterval = new RawTimeInterval("08:00", "09:00");

        RawSchedule rawSchedule = RawSchedule.builder()
                .courses(Set.of())
                .timeIntervals(Set.of(rawTimeInterval))
                .build();
        RawScheduleEvent event = RawScheduleEvent.builder()
                .scheduleId(scheduleId)
                .entity(rawSchedule)
                .build();

        ProcessedRawTimeInterval processedRawTimeInterval =
                new ProcessedRawTimeInterval(LocalTime.of(8, 0), LocalTime.of(9, 0));

        when(timeIntervalMapper.map(rawTimeInterval))
                .thenReturn(processedRawTimeInterval);

        // When & Then
        assertThatThrownBy(() -> underTest.handle(event))
                .isInstanceOf(EmptyProcessedRawCourseSetException.class);
    }

    @Test
    void GivenEventWithTimeIntervalAndCourse_WhenHandle_ThenProducesEvent() {
        // Given
        String scheduleId = UUID.randomUUID().toString();

        RawTimeInterval rawTimeInterval = new RawTimeInterval("08:00", "09:00");
        RawCourse rawCourse = RawCourse.builder()
                .top(259)
                .left(254)
                .height(135)
                .width(154)
                .text("course 1")
                .build();

        RawSchedule rawSchedule = RawSchedule.builder()
                .courses(Set.of(rawCourse))
                .timeIntervals(Set.of(rawTimeInterval))
                .build();
        RawScheduleEvent event = RawScheduleEvent.builder()
                .scheduleId(scheduleId)
                .entity(rawSchedule)
                .build();

        ProcessedRawTimeInterval processedRawTimeInterval =
                new ProcessedRawTimeInterval(LocalTime.of(8, 0), LocalTime.of(9, 0));
        ProcessedRawCourse processedRawCourse = ProcessedRawCourse.builder()
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(11, 45))
                .name("course 1")
                .courseType(CourseType.INFO)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .weekType(WeekType.EVERY)
                .build();

        when(timeIntervalMapper.map(rawTimeInterval))
                .thenReturn(processedRawTimeInterval);
        when(courseMapper.map(rawCourse, processedRawTimeInterval.start()))
                .thenReturn(processedRawCourse);

        // When
        underTest.handle(event);

        // Then
        ProcessedRawSchedule schedule = new ProcessedRawSchedule(Set.of(processedRawCourse));
        ProcessedRawScheduleEvent expectedEvent = ProcessedRawScheduleEvent.builder()
                .scheduleId(event.scheduleId())
                .entity(schedule)
                .build();

        verify(eventProducer).produce(expectedEvent);
    }

}