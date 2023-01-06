package com.github.karixdev.polslcoursescheduleapi.schedule;

import com.github.karixdev.polslcoursescheduleapi.course.Course;
import com.github.karixdev.polslcoursescheduleapi.course.CourseService;
import com.github.karixdev.polslcoursescheduleapi.course.Weeks;
import com.github.karixdev.polslcoursescheduleapi.course.payload.response.CourseResponse;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.PlanPolslService;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.TimeCell;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleRepository;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleService;
import com.github.karixdev.polslcoursescheduleapi.schedule.exception.ScheduleNameNotAvailableException;
import com.github.karixdev.polslcoursescheduleapi.schedule.exception.ScheduleNoStartTimeException;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.request.ScheduleRequest;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleCollectionResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleWithCoursesResponse;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.shared.exception.ResourceNotFoundException;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {
    @InjectMocks
    ScheduleService underTest;

    @Mock
    ScheduleRepository repository;

    @Mock
    PlanPolslService planPolslService;

    @Mock
    CourseService courseService;

    Schedule schedule;

    User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("email@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        schedule = Schedule.builder()
                .id(1L)
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule-name")
                .addedBy(user)
                .build();
    }

    @Test
    void GivenPayloadWithUnavailableName_WhenAdd_ThenThrowsScheduleNameNotAvailableException() {
        // Given
        ScheduleRequest payload = new ScheduleRequest(
                1,
                2,
                3,
                "schedule-name",
                4
        );
        UserPrincipal userPrincipal = new UserPrincipal(user);

        when(repository.findByName(eq("schedule-name")))
                .thenReturn(Optional.of(schedule));

        // When & Then
        assertThatThrownBy(() -> underTest.add(payload, userPrincipal))
                .isInstanceOf(ScheduleNameNotAvailableException.class)
                .hasMessage("Schedule name is not available");
    }

    @Test
    void GivenValidPayloadAndUserPrincipal_WhenAdd_ThenSavesScheduleAndReturnsCorrectScheduleResponse() {
        // Given
        ScheduleRequest payload = new ScheduleRequest(
                1,
                2,
                3,
                "schedule-name",
                4
        );
        UserPrincipal userPrincipal = new UserPrincipal(user);

        when(repository.findByName(eq("schedule-name")))
                .thenReturn(Optional.empty());

        when(repository.save(any()))
                .thenReturn(schedule);

        // When
        ScheduleResponse result = underTest.add(payload, userPrincipal);

        // Then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("schedule-name");
        assertThat(result.getSemester()).isEqualTo(2);
        assertThat(result.getGroupNumber()).isEqualTo(3);

        verify(repository).save(any());
    }

    @Test
    void GivenNotExistingScheduleId_WhenDelete_ThenThrowsResourceNotFoundExceptionWithCorrectMessage() {
        // Given
        Long id = 101L;

        when(repository.findById(eq(id)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Schedule with provided id not found");
    }

    @Test
    void GivenExistingScheduleId_WhenDelete_ThenDeletesSchedule() {
        // Given
        Long id = 1L;

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(schedule));

        // When
        underTest.delete(id);

        // Then
        verify(repository).delete(eq(schedule));
    }

    @Test
    void WhenUpdateSchedules_ThenCallsPlanPolslServiceAndCourseService() {
        Schedule otherSchedule = Schedule.builder()
                .id(2L)
                .type(0)
                .planPolslId(11)
                .semester(1)
                .groupNumber(4)
                .name("schedule-name")
                .addedBy(user)
                .build();

        when(repository.findAllSchedules())
                .thenReturn(List.of(schedule, otherSchedule));

        // When
        underTest.updateSchedules();

        // Then
        verify(planPolslService).getPlanPolslResponse(eq(schedule));
        verify(planPolslService).getPlanPolslResponse(eq(otherSchedule));

        verify(courseService, times(2)).updateScheduleCourses(any(), any());
    }

    @Test
    void WhenGetAll_ThenReturnsCorrectScheduleCollectionResponse() {
        when(repository.findAllOrderByGroupNumberAndSemesterAsc())
                .thenReturn(List.of(
                        Schedule.builder()
                                .id(101L)
                                .type(1)
                                .planPolslId(2)
                                .semester(1)
                                .name("schedule-1")
                                .groupNumber(2)
                                .addedBy(user)
                                .build(),
                        Schedule.builder()
                                .id(100L)
                                .type(1)
                                .planPolslId(2)
                                .semester(1)
                                .name("schedule-2")
                                .groupNumber(4)
                                .addedBy(user)
                                .build(),
                        Schedule.builder()
                                .id(99L)
                                .type(1)
                                .planPolslId(2)
                                .semester(3)
                                .name("schedule-3")
                                .groupNumber(4)
                                .addedBy(user)
                                .build()
                ));

        // When
        ScheduleCollectionResponse result = underTest.getAll();

        // Then
        assertThat(result.getSemesters().keySet()).hasSize(2);
        assertThat(result.getSemesters().keySet()).contains(1, 3);

        assertThat(result.getSemesters().get(1)).hasSize(2);
        assertThat(result.getSemesters().get(3)).hasSize(1);
    }

    @Test
    void GivenNotExistingScheduleId_WhenGetSchedulesWithCourses_ThenThrowsResourceNotFoundExceptionWithCorrectMessage() {
        // Given
        Long id = 1337L;

        when(repository.findScheduleById(eq(id)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.getSchedulesWithCourses(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Schedule with provided id not found");
    }

    @Test
    void GivenExistingScheduleId_WhenGetSchedulesWithCourses_ThenReturnsCorrectScheduleWithCoursesResponse() {
        schedule.setCourses(Set.of(
                Course.builder()
                        .schedule(schedule)
                        .weeks(Weeks.EVERY)
                        .description("course-1")
                        .dayOfWeek(DayOfWeek.THURSDAY)
                        .startsAt(LocalTime.of(10, 15))
                        .endsAt(LocalTime.of(11, 45))
                        .build(),
                Course.builder()
                        .schedule(schedule)
                        .weeks(Weeks.EVERY)
                        .description("course-2")
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .startsAt(LocalTime.of(10, 15))
                        .endsAt(LocalTime.of(11, 45))
                        .build(),
                Course.builder()
                        .schedule(schedule)
                        .weeks(Weeks.ODD)
                        .description("course-3")
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .startsAt(LocalTime.of(8, 30))
                        .endsAt(LocalTime.of(10, 0))
                        .build(),
                Course.builder()
                        .schedule(schedule)
                        .weeks(Weeks.EVERY)
                        .description("course-4")
                        .dayOfWeek(DayOfWeek.WEDNESDAY)
                        .startsAt(LocalTime.of(16, 30))
                        .endsAt(LocalTime.of(18, 0))
                        .build(),
                Course.builder()
                        .schedule(schedule)
                        .weeks(Weeks.EVERY)
                        .description("course-5")
                        .dayOfWeek(DayOfWeek.TUESDAY)
                        .startsAt(LocalTime.of(7, 30))
                        .endsAt(LocalTime.of(9, 0))
                        .build(),
                Course.builder()
                        .schedule(schedule)
                        .weeks(Weeks.ODD)
                        .description("course-6")
                        .dayOfWeek(DayOfWeek.FRIDAY)
                        .startsAt(LocalTime.of(8, 30))
                        .endsAt(LocalTime.of(10, 0))
                        .build()
        ));

        when(repository.findScheduleById(eq(schedule.getId())))
                .thenReturn(Optional.of(schedule));

        // When
        ScheduleWithCoursesResponse result = underTest.getSchedulesWithCourses(schedule.getId());

        // Then
        assertThat(result.getGroupNumber())
                .isEqualTo(schedule.getGroupNumber());
        assertThat(result.getId())
                .isEqualTo(schedule.getId());
        assertThat(result.getName())
                .isEqualTo(schedule.getName());
        assertThat(result.getSemester())
                .isEqualTo(schedule.getSemester());

        assertThat(result.getCourses().keySet())
                .hasSize(5);

        assertThat(result.getCourses().get(DayOfWeek.MONDAY))
                .hasSize(2);

        CourseResponse mondayCourse1 =
                result.getCourses().get(DayOfWeek.MONDAY).get(0);
        CourseResponse mondayCourse2 =
                result.getCourses().get(DayOfWeek.MONDAY).get(1);

        assertThat(mondayCourse1.getStartsAt())
                .isBefore(mondayCourse2.getStartsAt());
    }

    @Test
    void GivenNotExistingScheduleId_WhenGetById_ThenThrowsResourceNotFoundExceptionWithProperMessage() {
        // Given
        Long id = 1337L;

        when(repository.findById(eq(id)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Schedule with provided id not found");
    }

    @Test
    void GivenExistingScheduleId_WhenGetById_ThenReturnsCorrectObject() {
        // Given
        Long id = schedule.getId();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(schedule));

        // When
        Schedule result = underTest.getById(id);

        // Then
        assertThat(result).isEqualTo(schedule);
    }

    @Test
    void GivenNotExistingScheduleId_WhenManualUpdate_ThenThrowsResourceNotFoundExceptionWithCorrectMessage() {
        // Given
        Long id = 1337L;

        when(repository.findScheduleById(eq(id)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.manualUpdate(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Schedule with provided id not found");
    }

    @Test
    void GivenExistingSchedule_WhenManualUpdate_ThenThenCallsPlanPolslServiceAndCourseService() {
        // Given
        Long id = 2L;

        Schedule otherSchedule = Schedule.builder()
                .id(2L)
                .type(0)
                .planPolslId(11)
                .semester(1)
                .groupNumber(4)
                .name("schedule-name")
                .addedBy(user)
                .build();

        when(repository.findScheduleById(eq(id)))
                .thenReturn(Optional.of(otherSchedule));

        // When
        underTest.manualUpdate(id);

        // Then
        verify(planPolslService).getPlanPolslResponse(eq(otherSchedule));
        verify(planPolslService).getPlanPolslResponse(eq(otherSchedule));

        verify(courseService).updateScheduleCourses(any(), eq(otherSchedule));
    }
}
