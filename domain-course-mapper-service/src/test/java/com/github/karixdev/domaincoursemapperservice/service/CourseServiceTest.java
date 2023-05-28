package com.github.karixdev.domaincoursemapperservice.service;

import com.github.karixdev.domaincoursemapperservice.exception.NoScheduleStartTimeException;
import com.github.karixdev.domaincoursemapperservice.mapper.CourseCellMapper;
import com.github.karixdev.domaincoursemapperservice.mapper.TimeCellMapper;
import com.github.karixdev.domaincoursemapperservice.model.domain.Course;
import com.github.karixdev.domaincoursemapperservice.model.domain.CourseType;
import com.github.karixdev.domaincoursemapperservice.model.domain.WeekType;
import com.github.karixdev.domaincoursemapperservice.model.raw.CourseCell;
import com.github.karixdev.domaincoursemapperservice.model.raw.TimeCell;
import com.github.karixdev.domaincoursemapperservice.producer.DomainCoursesProducer;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    @InjectMocks
    CourseService underTest;

    @Mock
    CourseCellMapper courseCellMapper;

    @Mock
    TimeCellMapper timeCellMapper;

    @Mock
    DomainCoursesProducer producer;

    @Test
    void GivenEmptyTimeCellSet_WhenHandleRawCoursesMessage_ThenThrowsNoScheduleStartTimeException() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        Set<TimeCell> timeCells = Set.of();
        Set<CourseCell> courseCells = Set.of();

        // When & Then
        assertThatThrownBy(() -> underTest.handleRawCoursesMessage(scheduleId, timeCells, courseCells))
                .isInstanceOf(NoScheduleStartTimeException.class);
    }

    @Test
    void GivenScheduleIdTimeCellSetCourseCellSet_WhenHandleRawCoursesMessage_ThenMapsCourseCellsAndProducesDomainCourseMessage() {
        // Given
        UUID scheduleId = UUID.randomUUID();

        CourseCell courseCell = new CourseCell(
                259,
                254,
                135,
                154,
                "course 1"
        );
        Set<CourseCell> courseCells = Set.of(courseCell);

        TimeCell timeCell1 = new TimeCell("11:00-12:00");
        TimeCell timeCell2 = new TimeCell("08:30-10:00");
        Set<TimeCell> timeCells = Set.of(timeCell1, timeCell2);

        when(timeCellMapper.mapToLocalTime(eq(timeCell1)))
                .thenReturn(LocalTime.of(11, 0));

        when(timeCellMapper.mapToLocalTime(eq(timeCell2)))
                .thenReturn(LocalTime.of(8, 30));

        Course course = new Course(
                LocalTime.of(8, 30),
                LocalTime.of(11, 45),
                "course 1",
                CourseType.INFO,
                "",
                DayOfWeek.TUESDAY,
                WeekType.EVERY,
                "",
                null
        );

        when(courseCellMapper.mapToCourse(eq(courseCell), eq(LocalTime.of(8, 30))))
                .thenReturn(course);

        // When
        underTest.handleRawCoursesMessage(
                scheduleId,
                timeCells,
                courseCells
        );

        // Then
        verify(producer).produceDomainCourseMessage(
                eq(scheduleId),
                eq(Set.of(course))
        );
    }
}