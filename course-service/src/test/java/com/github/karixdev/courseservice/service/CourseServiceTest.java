package com.github.karixdev.courseservice.service;

import com.github.karixdev.courseservice.client.ScheduleClient;
import com.github.karixdev.courseservice.comparator.CourseComparator;
import com.github.karixdev.courseservice.dto.CourseRequest;
import com.github.karixdev.courseservice.dto.ScheduleResponse;
import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.entity.CourseType;
import com.github.karixdev.courseservice.entity.WeekType;
import com.github.karixdev.courseservice.exception.NotExistingScheduleException;
import com.github.karixdev.courseservice.exception.ResourceNotFoundException;
import com.github.karixdev.courseservice.mapper.CourseMapper;
import com.github.karixdev.courseservice.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
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

    @Mock
    CourseComparator courseComparator;

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
        CourseRequest courseRequest = exampleCourseRequest;

        when(scheduleClient.findById(eq(courseRequest.getScheduleId())))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.create(courseRequest))
                .isInstanceOf(NotExistingScheduleException.class);
    }

    @Test
    void GivenCourseRequest_WhenCreate_ThenRetrievesScheduleSavesCourseAndMapsEntityToResponse() {
        // Given
        CourseRequest courseRequest = exampleCourseRequest;

        Course course = exampleCourse;

        when(scheduleClient.findById(eq(courseRequest.getScheduleId())))
                .thenReturn(Optional.of(new ScheduleResponse(courseRequest.getScheduleId())));

        // When
        underTest.create(courseRequest);

        // Then
        verify(scheduleClient).findById(eq(courseRequest.getScheduleId()));
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
                .isInstanceOf(ResourceNotFoundException.class);
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

    @Test
    void GivenNotExistingCourseId_WhenDelete_ThenThrowsResourceNotFoundExceptionWithProperMessage() {
        // Given
        UUID id = UUID.randomUUID();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void GivenExistingCourseId_WhenDelete_ThenShouldDeleteCourse() {
        // Given
        UUID id = exampleCourse.getId();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(exampleCourse));

        // When
        underTest.delete(id);

        // Then
        verify(repository).delete(exampleCourse);
    }

    @Test
    void GivenExistingScheduleId_WhenFindScheduleCourses_ThenComparesCoursesAndMapsThemIntoResponse() {
        // Given
        UUID scheduleId = UUID.randomUUID();

        Course course1 = Course.builder()
                .name("Calculus I")
                .scheduleId(scheduleId)
                .courseType(CourseType.PRACTICAL)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.ODD)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        Course course2 = Course.builder()
                .name("Physics")
                .scheduleId(scheduleId)
                .courseType(CourseType.LAB)
                .dayOfWeek(DayOfWeek.MONDAY)
                .weekType(WeekType.EVEN)
                .startsAt(LocalTime.of(10, 30))
                .endsAt(LocalTime.of(12, 15))
                .build();

        Course course3 = Course.builder()
                .name("C++")
                .scheduleId(scheduleId)
                .courseType(CourseType.LECTURE)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(14, 30))
                .endsAt(LocalTime.of(16, 15))
                .build();

        when(repository.findByScheduleId(eq(scheduleId)))
                .thenReturn(List.of(course1, course2, course3));

        // When
        underTest.findCoursesBySchedule(scheduleId);

        // Then
        verify(courseComparator, times(2)).compare(any(), any());
        verify(courseMapper, times(3)).map(any());
    }

    @Test
    void GivenScheduleId_WhenHandleScheduleDelete_ThenDeletesAllCourseWithProvidedScheduleId() {
        // Given
        UUID scheduleId = exampleCourse.getScheduleId();

        when(repository.findByScheduleId(eq(scheduleId)))
                .thenReturn(List.of(exampleCourse));

        // When
        underTest.handleScheduleDelete(scheduleId);

        // Then
        verify(repository).deleteAll(eq(List.of(exampleCourse)));
    }
}