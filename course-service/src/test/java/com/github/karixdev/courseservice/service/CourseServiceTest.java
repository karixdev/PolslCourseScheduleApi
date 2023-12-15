package com.github.karixdev.courseservice.service;

import com.github.karixdev.commonservice.event.EventType;
import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import com.github.karixdev.commonservice.exception.HttpServiceClientException;
import com.github.karixdev.commonservice.exception.ResourceNotFoundException;
import com.github.karixdev.commonservice.exception.ValidationException;
import com.github.karixdev.courseservice.client.ScheduleClient;
import com.github.karixdev.courseservice.comparator.CourseComparator;
import com.github.karixdev.courseservice.dto.CourseRequest;
import com.github.karixdev.commonservice.dto.schedule.ScheduleResponse;
import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.entity.CourseType;
import com.github.karixdev.courseservice.entity.WeekType;
import com.github.karixdev.courseservice.mapper.CourseMapper;
import com.github.karixdev.courseservice.producer.ScheduleCoursesEventProducer;
import com.github.karixdev.courseservice.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.karixdev.courseservice.matcher.DeepCourseArgumentMatcher.deepCourseMatcher;
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

    @Mock
    ScheduleCoursesEventProducer producer;

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
    void GivenCourseRequestWithNotExistingSchedule_WhenCreate_ThenThrowsValidationException() {
        // Given
        CourseRequest courseRequest = exampleCourseRequest;

        when(scheduleClient.findById(courseRequest.getScheduleId()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.create(courseRequest))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void GivenCourseRequestScheduleIdSuchThatScheduleServiceClientExceptionIsThrown_WhenCreate_ThenThrowsValidationException() {
        // Given
        CourseRequest courseRequest = exampleCourseRequest;

        when(scheduleClient.findById(courseRequest.getScheduleId()))
                .thenThrow(HttpServiceClientException.class);

        // When & Then
        assertThatThrownBy(() -> underTest.create(courseRequest))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void GivenCourseRequest_WhenCreate_ThenRetrievesScheduleSavesCourseAndProducesCoursesMessageAndMapsEntityToResponse() {
        // Given
        CourseRequest courseRequest = exampleCourseRequest;

        Course course = exampleCourse;

        ScheduleResponse scheduleResponse = ScheduleResponse.builder()
                .id(course.getScheduleId())
                .build();
        when(scheduleClient.findById(courseRequest.getScheduleId()))
                .thenReturn(Optional.of(scheduleResponse));

        // When
        underTest.create(courseRequest);

        // Then
        verify(scheduleClient).findById(courseRequest.getScheduleId());
        verify(repository).save(argThat(deepCourseMatcher(course)));
        verify(courseMapper).map(argThat(deepCourseMatcher(course)));
        verify(producer).produceCreated(eq(course.getScheduleId()), any());

    }

    @Test
    void GivenNotExistingCourseId_WhenUpdate_ThenThrowsResourceNotFoundExceptionWithProperMessage() {
        // Given
        UUID id = UUID.randomUUID();
        CourseRequest request = CourseRequest.builder().build();

        when(repository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.update(id, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void GivenCourseRequestWithNotExistingSchedule_WhenUpdate_ThenThrowsValidationException() {
        // Given
        UUID id = UUID.randomUUID();

        CourseRequest courseRequest = CourseRequest.builder()
                .scheduleId(UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f3"))
                .build();

        Course course = Course.builder()
                .id(id)
                .scheduleId(UUID.fromString("158ed783-928c-4155-bee9-fdbaaadc15f2"))
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(course));
        when(scheduleClient.findById(courseRequest.getScheduleId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.update(id, courseRequest))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void GivenExistingCourseWithNewScheduleId_WhenUpdate_ThenVerifiesScheduleExistenceUpdatesCourseAndProducesCoursesMessageAndMapsItIntoResponse() {
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

        when(repository.findById(id)).thenReturn(Optional.of(course));

        ScheduleResponse scheduleResponse = ScheduleResponse.builder()
                .id(course.getScheduleId())
                .build();
        when(scheduleClient.findById(courseRequest.getScheduleId()))
                .thenReturn(Optional.of(scheduleResponse));

        // When
        underTest.update(id, courseRequest);

        // Then
        verify(repository).save(argThat(deepCourseMatcher(expectedCourse)));
        verify(courseMapper).map(argThat(deepCourseMatcher(expectedCourse)));
        verify(producer).produceUpdated(expectedCourse.getScheduleId(), Set.of(expectedCourse));
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

        when(repository.findById(id)).thenReturn(Optional.of(course));

        // When
        underTest.update(id, courseRequest);

        // Then
        verify(scheduleClient, never()).findById(course.getScheduleId());
        verify(repository).save(argThat(deepCourseMatcher(expectedCourse)));
        verify(courseMapper).map(expectedCourse);
    }

    @Test
    void GivenNotExistingCourseId_WhenDelete_ThenThrowsResourceNotFoundExceptionWithProperMessage() {
        // Given
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void GivenExistingCourseId_WhenDelete_ThenShouldDeleteCourseAndProducesCoursesMessage() {
        // Given
        UUID id = exampleCourse.getId();

        when(repository.findById(id)).thenReturn(Optional.of(exampleCourse));

        // When
        underTest.delete(id);

        // Then
        verify(repository).delete(argThat(deepCourseMatcher(exampleCourse)));
        verify(producer).produceDeleted(eq(exampleCourse.getScheduleId()), any());
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

        when(repository.findByScheduleId(scheduleId))
                .thenReturn(List.of(course1, course2, course3));

        // When
        underTest.findCoursesBySchedule(scheduleId);

        // Then
        verify(courseComparator, times(2)).compare(any(), any());
        verify(courseMapper, times(3)).map(any());
    }

    @Test
    void GivenScheduleAndSetOfRetrievedCourses_WhenUpdateScheduleCourses_ThenSavesAndDeletesProperCoursesAndProducesCoursesMessageWhereThereIsChangeInCourses() {
        // Given
        UUID scheduleId = UUID.randomUUID();

        Course course1 = Course.builder()
                .name("Calculus I")
                .scheduleId(scheduleId)
                .courseType(CourseType.PRACTICAL)
                .teachers("dr. Adam")
                .classroom("314MS")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.ODD)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        Course course2 = Course.builder()
                .name("Physics")
                .scheduleId(scheduleId)
                .courseType(CourseType.LAB)
                .teachers("dr. Max")
                .classroom("408MS")
                .dayOfWeek(DayOfWeek.MONDAY)
                .weekType(WeekType.EVEN)
                .startsAt(LocalTime.of(10, 30))
                .endsAt(LocalTime.of(12, 15))
                .build();

        Course course3 = Course.builder()
                .name("C++")
                .scheduleId(scheduleId)
                .courseType(CourseType.LECTURE)
                .teachers("dr. Henryk")
                .classroom("CEK Room C")
                .additionalInfo("contact teacher")
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(14, 30))
                .endsAt(LocalTime.of(16, 15))
                .build();

        Set<Course> retrievedCourses = Set.of(course1, course2);

        when(repository.findByScheduleId(scheduleId))
                .thenReturn(List.of(course2, course3));

        // When
        underTest.updateScheduleCourses(scheduleId, retrievedCourses);

        // Then
        verify(repository).deleteAll(Set.of(course3));
        verify(repository).saveAll(Set.of(course1));
        verify(producer).produceCreatedAndDeleted(eq(scheduleId), any(), any());
    }

    @Test
    void GivenScheduleAndSetOfRetrievedCourses_WhenUpdateScheduleCourses_ThenSavesAndDeletesProperCoursesAndDoesNotProduceCoursesMessageWhereThereIsNoChangeInCourses() {
        // Given
        UUID scheduleId = UUID.randomUUID();

        Course course1 = Course.builder()
                .name("Calculus I")
                .scheduleId(scheduleId)
                .courseType(CourseType.PRACTICAL)
                .teachers("dr. Adam")
                .classroom("314MS")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.ODD)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        Set<Course> retrievedCourses = Set.of(course1);

        when(repository.findByScheduleId(scheduleId))
                .thenReturn(List.of(course1));

        // When
        underTest.updateScheduleCourses(scheduleId, retrievedCourses);

        // Then
        verify(producer, never()).produceCreatedAndDeleted(any(), any(), any());
    }

    @ParameterizedTest
    @MethodSource("notSupportScheduleEventEventTypes")
    void GivenScheduleEventWithTypeOtherThanDelete_WhenHandleScheduleEvent_ThenDoNotPerformsEvent(EventType eventType) {
        // Given
        ScheduleEvent event = ScheduleEvent.builder()
                .eventType(eventType)
                .build();

        // When
        underTest.handleScheduleEvent(event);

        // Then
        verify(repository, never()).deleteAll(any());
    }

    @Test
    void GivenScheduleEventWithDeleteType_WhenHandleScheduleEvent_ThenDeletesAllCoursesHavingScheduleIdFromEvent() {
        // Given
        Course course = Course.builder().id(UUID.randomUUID()).build();
        UUID scheduleId = course.getId();

        ScheduleEvent event = ScheduleEvent.builder()
                .scheduleId(scheduleId.toString())
                .eventType(EventType.DELETE)
                .build();

        when(repository.findByScheduleId(scheduleId))
                .thenReturn(List.of(course));

        // When
        underTest.handleScheduleEvent(event);

        // Then
        verify(repository).deleteAll(List.of(course));
    }

    private static Stream<Arguments> notSupportScheduleEventEventTypes() {
        return Stream.of(
                Arguments.of(EventType.CREATE),
                Arguments.of(EventType.UPDATE)
        );
    }

}