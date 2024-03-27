package com.github.karixdev.scheduleservice.application.mapper;

import com.github.karixdev.scheduleservice.application.dto.PublicScheduleDTO;
import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PublicScheduleDTOMapperTest {

    PublicScheduleDTOMapper underTest = new PublicScheduleDTOMapper();

    @Test
    void GivenDomainSchedule_WhenMap_ThenReturnsCorrectDTO() {
        // Given
        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(2)
                .groupNumber(3)
                .major("schedule")
                .planPolslData(
                        PlanPolslData.builder()
                                .id(0)
                                .type(1)
                                .weekDays(2)
                                .build()
                )
                .build();

        // When
        PublicScheduleDTO result = underTest.map(schedule);

        // Then
        PublicScheduleDTO expected = PublicScheduleDTO.builder()
                .id(schedule.getId())
                .group(schedule.getGroupNumber())
                .build();

        assertThat(result).isEqualTo(expected);
    }

}