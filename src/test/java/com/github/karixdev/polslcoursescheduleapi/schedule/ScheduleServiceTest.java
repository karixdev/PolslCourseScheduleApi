package com.github.karixdev.polslcoursescheduleapi.schedule;

import com.github.karixdev.polslcoursescheduleapi.course.CourseService;
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

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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

        when(repository.findAll())
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
}
