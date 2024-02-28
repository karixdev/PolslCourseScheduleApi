package com.github.karixdev.scheduleservice.application.service;

import com.github.karixdev.commonservice.dto.schedule.ScheduleRequest;
import com.github.karixdev.commonservice.exception.ResourceNotFoundException;
import com.github.karixdev.commonservice.exception.ValidationException;
import com.github.karixdev.scheduleservice.application.service.ScheduleService;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.application.mapper.ScheduleMapper;
import com.github.karixdev.scheduleservice.application.event.producer.ScheduleEventProducer;
import com.github.karixdev.scheduleservice.infrastructure.dal.ScheduleRepository;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @InjectMocks
    ScheduleService underTest;

    @Mock
    ScheduleRepository repository;

    @Mock
    ScheduleEventProducer producer;

    @Mock
    ScheduleMapper mapper;

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
    void GivenScheduleRequestWithUnavailableScheduleName_WhenCreate_ThenThrowsValidationException() {
        // Given
        ScheduleRequest scheduleRequest = new ScheduleRequest(
                1,
                1999,
                1,
                "schedule",
                1,
                0
        );

        when(repository.findByName("schedule"))
                .thenReturn(Optional.of(schedule));

        // When & Then
        assertThatThrownBy(() -> underTest.create(scheduleRequest))
                .isInstanceOf(ValidationException.class);
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

        when(repository.findByName("available"))
                .thenReturn(Optional.empty());

        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .groupNumber(1)
                .name("available")
                .wd(4)
                .build();

        when(mapper.mapToEntity(scheduleRequest)).thenReturn(schedule);

        // When
        underTest.create(scheduleRequest);

        // Then
        verify(mapper).mapToResponse(schedule);
    }

    @Test
    void GivenNotExistingScheduleId_WhenFindById_ThenThrowsResourceNotFoundWithProperMessage() {
        // Given
        UUID id = UUID.randomUUID();

        when(repository.findById(id))
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

        when(repository.findById(id))
                .thenReturn(Optional.of(schedule));

        // When
        underTest.findById(id);

        // Then
        verify(mapper).mapToResponse(schedule);
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

        when(repository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> underTest.update(id, scheduleRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(String.format(
                        "Schedule with id %s not found",
                        id
                ));
    }

    @Test
    void GivenScheduleRequestWithUnavailableName_WhenUpdate_ThenThrowsValidationException() {
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

        when(repository.findById(id))
                .thenReturn(Optional.of(schedule));

        when(repository.findByName(scheduleRequest.name()))
                .thenReturn(Optional.of(Schedule.builder().build()));

        // When & Then
        assertThatThrownBy(() -> underTest.update(id, scheduleRequest))
                .isInstanceOf(ValidationException.class);
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

        when(repository.findById(id))
                .thenReturn(Optional.of(schedule));

        when(repository.findByName(scheduleRequest.name()))
                .thenReturn(Optional.of(schedule));

        // When
        underTest.update(id, scheduleRequest);

        // The
        verify(mapper).mapToResponse(schedule);
        verify(producer).produceScheduleUpdateEvent(schedule);
    }

    @Test
    void GivenNotExistingScheduleId_WhenDelete_ThenThrowsResourceNotFoundExceptionWithProperMessage() {
        // Given
        UUID id = UUID.randomUUID();

        when(repository.findById(id))
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

        when(repository.findById(id))
                .thenReturn(Optional.of(schedule));

        // When
        underTest.delete(id);

        // Then
        verify(repository).delete(schedule);
        verify(producer).produceScheduleDeleteEvent(schedule);
    }

    @Test
    void GivenNotExistingScheduleId_WhenRequestScheduleCoursesUpdate_ThenThrowsResourceNotFoundExceptionWithProperMessage() {
        // Given
        UUID id = UUID.randomUUID();

        when(repository.findById(id))
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

        when(repository.findById(id))
                .thenReturn(Optional.of(schedule));

        // When
        underTest.requestScheduleCoursesUpdate(id);

        // Then
        verify(producer).produceScheduleUpdateEvent(schedule);
    }

    @Test
    void GivenEmptyIdsArray_WhenFindAll_ThenMapsAllEntitiesToResponses() {
        // Given
        Set<UUID> ids = Set.of();

        Schedule schedule1 = Schedule.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule-1")
                .build();
        Schedule schedule2 = Schedule.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(2)
                .semester(2)
                .groupNumber(3)
                .name("schedule-2")
                .build();
        Schedule schedule3 = Schedule.builder()
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
        underTest.findAll(ids);

        // Then
        verify(mapper).mapToResponse(schedule1);
        verify(mapper).mapToResponse(schedule2);
        verify(mapper).mapToResponse(schedule3);
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
        underTest.findAll(ids);

        // Then
        verify(mapper).mapToResponse(schedule1);
        verify(mapper, never()).mapToResponse(schedule2);
        verify(mapper).mapToResponse(schedule3);
    }

}
