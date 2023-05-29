package com.github.karixdev.scheduleservice.service;

import com.github.karixdev.scheduleservice.dto.ScheduleRequest;
import com.github.karixdev.scheduleservice.dto.ScheduleResponse;
import com.github.karixdev.scheduleservice.entity.Schedule;
import com.github.karixdev.scheduleservice.exception.ResourceNotFoundException;
import com.github.karixdev.scheduleservice.exception.ScheduleNameUnavailableException;
import com.github.karixdev.scheduleservice.message.ScheduleEventType;
import com.github.karixdev.scheduleservice.producer.ScheduleEventProducer;
import com.github.karixdev.scheduleservice.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {
    @InjectMocks
    ScheduleService underTest;

    @Mock
    ScheduleRepository repository;

    @Mock
    ScheduleEventProducer producer;

    Schedule schedule;

    @BeforeEach
    void setUp() {
        schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .type(1)
                .planPolslId(1)
                .semester(1)
                .groupNumber(1)
                .name("schedule")
                .wd(4)
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
    }

    @Test
    void GivenValidScheduleRequest_WhenCreate_ThenSavesScheduleAndProducesScheduleCreationEventAndReturnsProperScheduleResponse() {
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

        verify(producer).produceScheduleEventMessage(
                eq(schedule),
                eq(ScheduleEventType.CREATE)
        );
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

        verify(producer).produceScheduleEventMessage(
                eq(schedule),
                eq(ScheduleEventType.UPDATE)
        );
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
    void GivenExistingScheduleId_WhenDelete_ThenDeletesScheduleAndSendScheduleDeletionMessage() {
        // Given
        UUID id = schedule.getId();

        when(repository.findById(eq(id)))
                .thenReturn(Optional.of(schedule));

        // When
        underTest.delete(id);

        // Then
        verify(repository).delete(eq(schedule));

        verify(producer).produceScheduleEventMessage(
                eq(schedule),
                eq(ScheduleEventType.DELETE)
        );
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
        verify(producer).produceScheduleEventMessage(
                eq(schedule),
                eq(ScheduleEventType.UPDATE)
        );
    }

    @Test
    void GivenEmptyIdsArray_WhenFindAll_ThenMapsAllEntitiesToResponses() {
        // Given
        Set<UUID> ids = Set.of();

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

        when(repository.findAllOrderBySemesterAndGroupNumberAsc())
                .thenReturn(List.of(schedule1, schedule2, schedule3));

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

        Set<UUID> ids = Set.of(schedule1.getId(), schedule3.getId());

        when(repository.findAllOrderBySemesterAndGroupNumberAsc())
                .thenReturn(List.of(schedule1, schedule2, schedule3));

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
