package com.github.karixdev.courseservice.service;

import com.github.karixdev.courseservice.client.ScheduleClient;
import com.github.karixdev.courseservice.dto.CourseRequest;
import com.github.karixdev.courseservice.dto.ScheduleResponse;
import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.entity.CourseType;
import com.github.karixdev.courseservice.entity.WeekType;
import com.github.karixdev.courseservice.exception.NotExistingScheduleException;
import com.github.karixdev.courseservice.exception.ResourceNotFoundException;
import com.github.karixdev.courseservice.mapper.CourseMapper;
import com.github.karixdev.courseservice.repository.CourseRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;

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
    CourseRequest exampleCourseRequest;

    @BeforeEach
    void setUp() {
        UUID scheduleId = UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f3");

        exampleCourseRequest = CourseRequest.builder()
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

        exampleCourse = Course.builder()
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
    }

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

    @Test
    void GivenNotExistingCourseId_WhenUpdate_ThenThrowsResourceNotFoundExceptionWithProperMessage() {
        // Given
        UUID id = UUID.randomUUID();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.update(id, CourseRequest.builder().build()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Course with id %s not found".formatted(id));
    }

    @Test
    void GivenCourseRequestWithNotExistingSchedule_WhenUpdate_ThenThrowsNotExistingScheduleException() {
        // Given
        UUID id = UUID.randomUUID();

        CourseRequest courseRequest = CourseRequest.builder()
                .scheduleId(UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f3"))
                .build();

        Course course = Course.builder()
                .id(id)
                .scheduleId(UUID.fromString("158ed783-928c-4155-bee9-fdbaaadc15f2"))
                .build();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(course));

        when(scheduleClient.findById(eq(courseRequest.getScheduleId())))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.update(id, courseRequest))
                .isInstanceOf(NotExistingScheduleException.class);
    }

    @Test
    void GivenExistingCourseWithNewScheduleId_WhenUpdate_ThenVerifiesScheduleExistenceUpdatesCourseAndMapsItIntoResponse() {
        // Given
        Course course = exampleCourse;
        UUID id = course.getId();

        CourseRequest courseRequest = CourseRequest.builder()
                .scheduleId(UUID.fromString("158ed783-928c-4155-bee9-fdbaaadc15f2"))
                .name("Updated Test Course")
                .startsAt(LocalTime.of(10, 0))
                .endsAt(LocalTime.of(11, 0))
                .courseType(CourseType.LAB)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .weekType(WeekType.ODD)
                .teachers("Jane Smith")
                .classrooms("B101")
                .additionalInfo("Updated Test Additional Info")
                .build();

        Course expectedCourse = Course.builder()
                .id(course.getId())
                .name(courseRequest.getName())
                .courseType(courseRequest.getCourseType())
                .dayOfWeek(courseRequest.getDayOfWeek())
                .weekType(courseRequest.getWeekType())
                .startsAt(courseRequest.getStartsAt())
                .endsAt(courseRequest.getEndsAt())
                .classroom(courseRequest.getClassrooms())
                .teachers(courseRequest.getTeachers())
                .additionalInfo(courseRequest.getAdditionalInfo())
                .scheduleId(courseRequest.getScheduleId())
                .build();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(course));

        when(scheduleClient.findById(eq(courseRequest.getScheduleId())))
                .thenReturn(Optional.of(new ScheduleResponse(courseRequest.getScheduleId())));

        // When
        underTest.update(id, courseRequest);

        // Then
        verify(repository).save(eq(expectedCourse));
        verify(courseMapper).map(eq(expectedCourse));
    }

    @Test
    void GivenExistingCourseWithNotNewScheduleId_WhenUpdate_ThenDoesNotVerifyScheduleExistenceUpdatesCourseAndMapsItIntoResponse() {
        // Given
        Course course = exampleCourse;
        UUID id = course.getId();

        CourseRequest courseRequest = CourseRequest.builder()
                .scheduleId(course.getScheduleId())
                .name("Updated Test Course")
                .startsAt(LocalTime.of(10, 0))
                .endsAt(LocalTime.of(11, 0))
                .courseType(CourseType.LAB)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .weekType(WeekType.ODD)
                .teachers("Jane Smith")
                .classrooms("B101")
                .additionalInfo("Updated Test Additional Info")
                .build();

        Course expectedCourse = Course.builder()
                .id(course.getId())
                .name(courseRequest.getName())
                .courseType(courseRequest.getCourseType())
                .dayOfWeek(courseRequest.getDayOfWeek())
                .weekType(courseRequest.getWeekType())
                .startsAt(courseRequest.getStartsAt())
                .endsAt(courseRequest.getEndsAt())
                .classroom(courseRequest.getClassrooms())
                .teachers(courseRequest.getTeachers())
                .additionalInfo(courseRequest.getAdditionalInfo())
                .scheduleId(courseRequest.getScheduleId())
                .build();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(course));

        // When
        underTest.update(id, courseRequest);

        // Then
        verify(scheduleClient, never()).findById(eq(course.getScheduleId()));
        verify(repository).save(eq(expectedCourse));
        verify(courseMapper).map(eq(expectedCourse));
    }
}