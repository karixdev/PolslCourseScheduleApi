package com.github.karixdev.courseservice.service;

import com.github.karixdev.courseservice.client.ScheduleClient;
import com.github.karixdev.courseservice.dto.CourseRequest;
import com.github.karixdev.courseservice.dto.ScheduleResponse;
import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.entity.CourseType;
import com.github.karixdev.courseservice.entity.WeekType;
import com.github.karixdev.courseservice.exception.NotExistingScheduleException;
import com.github.karixdev.courseservice.mapper.CourseMapper;
import com.github.karixdev.courseservice.repository.CourseRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    @InjectMocks
    CourseService underTest;

    @Mock
    CourseRepository repository;

    @Mock
    ScheduleClient scheduleClient;

    @Mock
    CourseMapper courseMapper;

    Course exampleCourse;

    @Test
    void GivenCourseRequestWithNotExistingSchedule_WhenCreate_ThenThrowsNotExistingScheduleException() {
        // Given
        UUID scheduleId = UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f3");
        CourseRequest courseRequest = CourseRequest.builder()
                .scheduleId(scheduleId)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .courseType(CourseType.LAB)
                .teachers("dr Adam, dr Marcin")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .classrooms("LAB 1")
                .additionalInfo("Only on 8.03")
                .build();

        when(scheduleClient.findById(eq(scheduleId)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.create(courseRequest))
                .isInstanceOf(NotExistingScheduleException.class);
    }

    @Test
    void GivenCourseRequest_WhenCreate_ThenRetrievesScheduleSavesCourseAndMapsEntityToResponse() {
        // Given
        UUID scheduleId = UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f3");
        CourseRequest courseRequest = CourseRequest.builder()
                .scheduleId(scheduleId)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .courseType(CourseType.LAB)
                .teachers("dr Adam, dr Marcin")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .classrooms("LAB 1")
                .additionalInfo("Only on 8.03")
                .build();

        Course course = Course.builder()
                .scheduleId(scheduleId)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .courseType(CourseType.LAB)
                .teachers("dr Adam, dr Marcin")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .classroom("LAB 1")
                .additionalInfo("Only on 8.03")
                .build();

        when(scheduleClient.findById(eq(scheduleId)))
                .thenReturn(Optional.of(new ScheduleResponse(scheduleId)));

        // When
        underTest.create(courseRequest);

        // Then
        verify(scheduleClient).findById(eq(scheduleId));
        verify(repository).save(eq(course));
        verify(courseMapper).map(eq(course));
    }
}