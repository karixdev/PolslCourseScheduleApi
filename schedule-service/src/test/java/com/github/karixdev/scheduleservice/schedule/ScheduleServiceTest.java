package com.github.karixdev.scheduleservice.schedule;

import com.github.karixdev.scheduleservice.course.*;
import com.github.karixdev.scheduleservice.schedule.dto.ScheduleRequest;
import com.github.karixdev.scheduleservice.schedule.dto.ScheduleResponse;
import com.github.karixdev.scheduleservice.schedule.exception.ScheduleNameUnavailableException;
import com.github.karixdev.scheduleservice.shared.exception.ResourceNotFoundException;
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
import java.util.Set;
import java.util.UUID;

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
    ScheduleProducer producer;

    @Mock
    CourseComparator courseComparator;

    @Mock
    CourseMapper courseMapper;

    Schedule schedule;

    @BeforeEach
    void setUp() {
        schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule")
                .build();
    }

    @Test
    void GivenScheduleRequestWithUnavailableScheduleName_WhenCreate_ThenThrowsScheduleNameUnavailableExceptionWithProperMessage() {
        // Given
        ScheduleRequest scheduleRequest = new ScheduleRequest(
                1,
                1999,
                1,
                "schedule",
                1,
                0
        );

        when(repository.findByName(eq("schedule")))
                .thenReturn(Optional.of(schedule));

        // When & Then
        assertThatThrownBy(() -> underTest.create(scheduleRequest))
                .isInstanceOf(ScheduleNameUnavailableException.class)
                .hasMessage("name schedule is unavailable");

        verify(producer, never()).sendScheduleUpdateRequest(any());
    }

    @Test
    void GivenValidScheduleRequest_WhenCreate_ThenSavesScheduleAndReturnsProperScheduleResponse() {
        // Given
        ScheduleRequest scheduleRequest = new ScheduleRequest(
                1,
                1999,
                1,
                "available",
                1,
                4
        );

        when(repository.findByName(eq("available")))
                .thenReturn(Optional.empty());

        Schedule savedSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .groupNumber(1)
                .name("available")
                .wd(4)
                .build();

        when(repository.save(eq(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .groupNumber(1)
                .name("available")
                .wd(4)
                .build())))
                .thenReturn(savedSchedule);

        // When
        ScheduleResponse result = underTest.create(scheduleRequest);

        // Then
        assertThat(result.semester())
                .isEqualTo(scheduleRequest.semester());
        assertThat(result.name())
                .isEqualTo(scheduleRequest.name());
        assertThat(result.groupNumber())
                .isEqualTo(scheduleRequest.groupNumber());
        assertThat(result.id())
                .isEqualTo(savedSchedule.getId());

        verify(producer).sendScheduleUpdateRequest(eq(savedSchedule));
    }

    @Test
    void GivenNotExistingScheduleId_WhenFindById_ThenThrowsResourceNotFoundWithProperMessage() {
        // Given
        UUID id = UUID.randomUUID();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(
                        "Schedule with id %s not found",
                        id
                ));
    }

    @Test
    void GivenExistingScheduleId_WhenFindById_ThenReturnsProperScheduleResponse() {
        // Given
        UUID id = schedule.getId();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(schedule));

        // When
        ScheduleResponse result = underTest.findById(id);

        // Then
        assertThat(result.groupNumber()).isEqualTo(schedule.getGroupNumber());
        assertThat(result.name()).isEqualTo(schedule.getName());
        assertThat(result.id()).isEqualTo(schedule.getId());
        assertThat(result.semester()).isEqualTo(schedule.getSemester());
    }

    @Test
    void GivenNotExistingScheduleId_WhenUpdate_ThenThrowsResourceNotFoundExceptionWithProperMessage() {
        // Given
        UUID id = UUID.randomUUID();
        ScheduleRequest scheduleRequest = new ScheduleRequest(
                1,
                1999,
                1,
                "schedule-name",
                1,
                0
        );

        when(repository.findById(eq(id)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.update(id, scheduleRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(
                        "Schedule with id %s not found",
                        id
                ));

        verify(producer, never()).sendScheduleUpdateRequest(any());
    }

    @Test
    void GivenScheduleRequestWithUnavailableName_WhenUpdate_ThenThrowsScheduleNameUnavailableExceptionWithProperMessage() {
        // Given
        UUID id = UUID.randomUUID();
        ScheduleRequest scheduleRequest = new ScheduleRequest(
                1,
                1999,
                1,
                "schedule-name",
                1,
                0
        );

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(schedule));

        when(repository.findByName(eq(scheduleRequest.name())))
                .thenReturn(Optional.of(Schedule.builder()
                        .id(UUID.randomUUID())
                        .type(0)
                        .planPolslId(101)
                        .semester(2)
                        .groupNumber(3)
                        .name("schedule-name")
                        .build()));

        // When & Then
        assertThatThrownBy(() -> underTest.update(id, scheduleRequest))
                .isInstanceOf(ScheduleNameUnavailableException.class)
                .hasMessage("name schedule-name is unavailable");

        verify(producer, never()).sendScheduleUpdateRequest(any());
    }

    @Test
    void GivenValidIdAndScheduleRequest_WhenUpdate_ThenUpdatesAndReturnsProperScheduleResponse() {
        // Given
        UUID id = UUID.randomUUID();
        ScheduleRequest scheduleRequest = new ScheduleRequest(
                9,
                1410,
                7,
                "schedule-name",
                8,
                0
        );

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(schedule));

        when(repository.findByName(eq(scheduleRequest.name())))
                .thenReturn(Optional.of(schedule));

        // When
        ScheduleResponse result = underTest.update(id, scheduleRequest);

        // Then
        verify(repository).save(eq(schedule));

        assertThat(schedule.getName())
                .isEqualTo(scheduleRequest.name());
        assertThat(schedule.getType())
                .isEqualTo(scheduleRequest.type());
        assertThat(schedule.getPlanPolslId())
                .isEqualTo(scheduleRequest.planPolslId());
        assertThat(schedule.getSemester())
                .isEqualTo(scheduleRequest.semester());
        assertThat(schedule.getGroupNumber())
                .isEqualTo(scheduleRequest.groupNumber());

        assertThat(result.name())
                .isEqualTo(scheduleRequest.name());
        assertThat(result.semester())
                .isEqualTo(scheduleRequest.semester());
        assertThat(result.groupNumber())
                .isEqualTo(scheduleRequest.groupNumber());

        verify(producer).sendScheduleUpdateRequest(eq(schedule));
    }

    @Test
    void GivenNotExistingScheduleId_WhenDelete_ThenThrowsResourceNotFoundExceptionWithProperMessage() {
        // Given
        UUID id = UUID.randomUUID();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(
                        "Schedule with id %s not found",
                        id
                ));
    }

    @Test
    void GivenExistingScheduleId_WhenDelete_ThenDeletesSchedule() {
        // Given
        UUID id = schedule.getId();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(schedule));

        // When
        underTest.delete(id);

        // Then
        verify(repository).delete(eq(schedule));
    }

    @Test
    void GivenNotExistingScheduleId_WhenRequestScheduleCoursesUpdate_ThenThrowsResourceNotFoundExceptionWithProperMessage() {
        // Given
        UUID id = UUID.randomUUID();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.requestScheduleCoursesUpdate(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(
                        "Schedule with id %s not found",
                        id
                ));
    }

    @Test
    void GivenExistingScheduleId_WhenRequestScheduleCoursesUpdate_ThenSendsMessageToMQ() {
        // Given
        UUID id = UUID.randomUUID();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(schedule));

        // When
        underTest.requestScheduleCoursesUpdate(id);

        // Then
        verify(producer).sendScheduleUpdateRequest(schedule);
    }

    @Test
    void GivenNotExistingScheduleId_WhenFindScheduleCourses_ThenThrowsResourceNotFoundExceptionWithProperMessage() {
        // Given
        UUID id = UUID.randomUUID();

        when(repository.findByIdWithCourses(eq(id)))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.findScheduleCourses(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Schedule with id %s not found".formatted(id));
    }

    @Test
    void GivenExistingScheduleId_WhenFindScheduleCourses_ThenComparesCoursesAndMapsThemIntoResponse() {
        // Given
        UUID id = UUID.randomUUID();

        Course course1 = Course.builder()
                .name("Calculus I")
                .courseType(CourseType.PRACTICAL)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.ODD)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        Course course2 = Course.builder()
                .name("Physics")
                .courseType(CourseType.LAB)
                .dayOfWeek(DayOfWeek.MONDAY)
                .weekType(WeekType.EVEN)
                .startsAt(LocalTime.of(10, 30))
                .endsAt(LocalTime.of(12, 15))
                .build();

        Course course3 = Course.builder()
                .name("C++")
                .courseType(CourseType.LECTURE)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(14, 30))
                .endsAt(LocalTime.of(16, 15))
                .build();

        schedule.setCourses(Set.of(course1, course2, course3));

        when(repository.findByIdWithCourses(eq(id)))
                .thenReturn(Optional.of(schedule));

        // When
        underTest.findScheduleCourses(id);

        // Then
        verify(courseComparator, times(2)).compare(any(), any());
        verify(courseMapper, times(3)).map(any());
    }

    @Test
    void GivenEmptyIdsArray_WhenFindAll_ThenMapsAllEntitiesToResponses() {
        // Given
        UUID[] ids = {};

        var schedule1 = Schedule.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule-1")
                .build();
        var schedule2 = Schedule.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(2)
                .semester(2)
                .groupNumber(3)
                .name("schedule-2")
                .build();
        var schedule3 = Schedule.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(3)
                .semester(2)
                .groupNumber(3)
                .name("schedule-3")
                .build();

        when(repository.findAllOrderBySemesterAndGroupNumberAsc()).thenReturn(List.of(schedule1, schedule2, schedule3));

        // When
        List<ScheduleResponse> result = underTest.findAll(ids);

        // Then
        assertThat(result).contains(
                new ScheduleResponse(
                        schedule1.getId(),
                        schedule1.getSemester(),
                        schedule1.getName(),
                        schedule1.getGroupNumber()
                )
        );

        assertThat(result).contains(
                new ScheduleResponse(
                        schedule2.getId(),
                        schedule2.getSemester(),
                        schedule2.getName(),
                        schedule2.getGroupNumber()
                )
        );

        assertThat(result).contains(
                new ScheduleResponse(
                        schedule3.getId(),
                        schedule3.getSemester(),
                        schedule3.getName(),
                        schedule3.getGroupNumber()
                )
        );
    }

    @Test
    void GivenNotEmptyIdsArray_WhenFindAll_ThenMapsSelectedEntitiesToResponses() {
        // Given
        var schedule1 = Schedule.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule-1")
                .build();
        var schedule2 = Schedule.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(2)
                .semester(2)
                .groupNumber(3)
                .name("schedule-2")
                .build();
        var schedule3 = Schedule.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(3)
                .semester(2)
                .groupNumber(3)
                .name("schedule-3")
                .build();

        UUID[] ids = { schedule1.getId(), schedule3.getId() };

        when(repository.findAllOrderBySemesterAndGroupNumberAsc()).thenReturn(List.of(schedule1, schedule2, schedule3));

        // When
        List<ScheduleResponse> result = underTest.findAll(ids);

        // Then
        assertThat(result).contains(
                new ScheduleResponse(
                        schedule1.getId(),
                        schedule1.getSemester(),
                        schedule1.getName(),
                        schedule1.getGroupNumber()
                )
        );

        assertThat(result).doesNotContain(
                new ScheduleResponse(
                        schedule2.getId(),
                        schedule2.getSemester(),
                        schedule2.getName(),
                        schedule2.getGroupNumber()
                )
        );

        assertThat(result).contains(
                new ScheduleResponse(
                        schedule3.getId(),
                        schedule3.getSemester(),
                        schedule3.getName(),
                        schedule3.getGroupNumber()
                )
        );
    }
}
