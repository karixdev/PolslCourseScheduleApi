package com.github.karixdev.webscraperservice.service;

import com.github.karixdev.webscraperservice.exception.EmptyCourseCellsSetException;
import com.github.karixdev.webscraperservice.exception.EmptyTimeCellSetException;
import com.github.karixdev.webscraperservice.message.ScheduleEventMessage;
import com.github.karixdev.webscraperservice.model.CourseCell;
import com.github.karixdev.webscraperservice.model.PlanPolslResponse;
import com.github.karixdev.webscraperservice.model.TimeCell;
import com.github.karixdev.webscraperservice.producer.RawCoursesProducer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {
    @InjectMocks
    ScheduleService underTest;

    @Mock
    PlanPolslService planPolslService;

    @Mock
    RawCoursesProducer producer;

    @Test
    void GivenScheduleEventMessageThatResultsWithEmptyCourseCellsSet_WhenHandleScheduleCreateAndUpdate_ThenThrowsEmptyCourseCellsSetException() {
        // Given
        ScheduleEventMessage message = new ScheduleEventMessage(
                UUID.randomUUID(),
                0,
                1337,
                4
        );

        when(planPolslService.getSchedule(
                eq(message.planPolslId()),
                eq(message.type()),
                eq(message.wd())
        )).thenReturn(new PlanPolslResponse(Set.of(), Set.of()));

        // When & Then
        Assertions.assertThatThrownBy(() -> underTest.handleScheduleCreateAndUpdate(message))
                .isInstanceOf(EmptyCourseCellsSetException.class);
    }

    @Test
    void GivenScheduleEventMessageThatResultsWithEmptyTimeCellsSet_WhenHandleScheduleCreateAndUpdate_ThenThrowsEmptyTimeCellSetException() {
        // Given
        ScheduleEventMessage message = new ScheduleEventMessage(
                UUID.randomUUID(),
                0,
                1337,
                4
        );

        CourseCell courseCell = new CourseCell(
                10,
                10,
                10,
                10,
                "text"
        );

        when(planPolslService.getSchedule(
                eq(message.planPolslId()),
                eq(message.type()),
                eq(message.wd())
        )).thenReturn(new PlanPolslResponse(Set.of(), Set.of(courseCell)));

        // When & Then
        Assertions.assertThatThrownBy(() -> underTest.handleScheduleCreateAndUpdate(message))
                .isInstanceOf(EmptyTimeCellSetException.class);
    }

    @Test
    void GivenScheduleEventMessage_WhenHandleScheduleCreateAndUpdate_ThenProducesRawCoursesMessage() {
        // Given
        ScheduleEventMessage message = new ScheduleEventMessage(
                UUID.randomUUID(),
                0,
                1337,
                4
        );

        CourseCell courseCell = new CourseCell(
                10,
                10,
                10,
                10,
                "text"
        );
        TimeCell timeCell = new TimeCell("08:30-10:00");

        when(planPolslService.getSchedule(
                eq(message.planPolslId()),
                eq(message.type()),
                eq(message.wd())
        )).thenReturn(new PlanPolslResponse(
                Set.of(timeCell),
                Set.of(courseCell)
        ));

        // When
        underTest.handleScheduleCreateAndUpdate(message);

        // Then
        verify(producer).produceRawCoursesMessage(
                eq(message.scheduleId()),
                eq(Set.of(courseCell)),
                eq(Set.of(timeCell))
        );
    }
}
