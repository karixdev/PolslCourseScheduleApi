package com.github.karixdev.scheduleservice.schedule;

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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {
    @InjectMocks
    ScheduleService underTest;

    @Mock
    ScheduleRepository repository;

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
                1
        );

        when(repository.findByName(eq("schedule")))
                .thenReturn(Optional.of(schedule));

        // When & Then
        assertThatThrownBy(() -> underTest.create(scheduleRequest))
                .isInstanceOf(ScheduleNameUnavailableException.class)
                .hasMessage("name schedule is unavailable");
    }

    @Test
    void GivenValidScheduleRequest_WhenCreate_ThenSavesScheduleAndReturnsProperScheduleResponse() {
        // Given
        ScheduleRequest scheduleRequest = new ScheduleRequest(
                1,
                1999,
                1,
                "available",
                1
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
                .build();

        when(repository.save(eq(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .groupNumber(1)
                .name("available")
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
}
