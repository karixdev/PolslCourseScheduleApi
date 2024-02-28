package com.github.karixdev.scheduleservice.application.mapper;

import com.github.karixdev.commonservice.dto.schedule.ScheduleRequest;
import com.github.karixdev.commonservice.dto.schedule.ScheduleResponse;
import com.github.karixdev.scheduleservice.application.mapper.ScheduleMapper;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleMapperTest {

    ScheduleMapper underTest = new ScheduleMapper();

    @Test
    void GivenScheduleRequest_WhenMapToEntity_ThenCorrectEntityIsReturned() {
        // Given
        ScheduleRequest request = ScheduleRequest.builder()
                .type(0)
                .planPolslId(1234)
                .semester(1)
                .name("Math")
                .groupNumber(1)
                .wd(4)
                .build();

        // When
        Schedule result = underTest.mapToEntity(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(request.type());
        assertThat(result.getPlanPolslId()).isEqualTo(request.planPolslId());
        assertThat(result.getSemester()).isEqualTo(request.semester());
        assertThat(result.getName()).isEqualTo(request.name());
        assertThat(result.getGroupNumber()).isEqualTo(request.groupNumber());
        assertThat(result.getWd()).isEqualTo(request.wd());
    }

    @Test
    void GivenSchedule_WhenMapToResponse_ThenCorrectDTOIsReturned() {
        // Given
        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(2)
                .name("Physics")
                .groupNumber(2)
                .build();

        // When
        ScheduleResponse result = underTest.mapToResponse(schedule);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(schedule.getId());
        assertThat(result.semester()).isEqualTo(schedule.getSemester());
        assertThat(result.name()).isEqualTo(schedule.getName());
        assertThat(result.groupNumber()).isEqualTo(schedule.getGroupNumber());
    }

}