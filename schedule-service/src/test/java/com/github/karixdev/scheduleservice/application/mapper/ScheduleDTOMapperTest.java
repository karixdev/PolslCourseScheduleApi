package com.github.karixdev.scheduleservice.application.mapper;

import com.github.karixdev.scheduleservice.application.dto.PlanPolslDataDTO;
import com.github.karixdev.scheduleservice.application.dto.ScheduleDTO;
import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleDTOMapperTest {

    @InjectMocks
    ScheduleDTOMapper underTest;

    @Mock
    ModelMapper<PlanPolslData, PlanPolslDataDTO> planPolslDataDTOMapper;

    @Test
    void GivenScheduleDomainEntity_WhenMap_ThenReturnsCorrectDTO() {
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

        when(planPolslDataDTOMapper.map(schedule.getPlanPolslData()))
                .thenReturn(PlanPolslDataDTO.builder().build());

        // When
        ScheduleDTO result = underTest.map(schedule);

        // Then
        assertThat(result.id()).isEqualTo(schedule.getId());
        assertThat(result.semester()).isEqualTo(schedule.getSemester());
        assertThat(result.groupNumber()).isEqualTo(schedule.getGroupNumber());
        assertThat(result.major()).isEqualTo(schedule.getMajor());
    }

}