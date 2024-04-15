package com.github.karixdev.scheduleservice.application.mapper;

import com.github.karixdev.scheduleservice.application.dto.PlanPolslDataDTO;
import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlanPolslDataDTOMapperTest {

    PlanPolslDataDTOMapper underTest;

    @BeforeEach
    void setUp() {
        underTest = new PlanPolslDataDTOMapper();
    }

    @Test
    void GivenPlanPolslDataDomainEntity_WhenMap_ThenReturnsCorrectDTO() {
        // Given
        PlanPolslData planPolslData = PlanPolslData.builder()
                .id(0)
                .type(1)
                .weekDays(2)
                .build();

        // When
        PlanPolslDataDTO result = underTest.map(planPolslData);

        // Then
        assertThat(result.id()).isEqualTo(planPolslData.getId());
        assertThat(result.type()).isEqualTo(planPolslData.getType());
        assertThat(result.weekDays()).isEqualTo(planPolslData.getWeekDays());
    }

}