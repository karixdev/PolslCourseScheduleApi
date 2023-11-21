package com.github.karixdev.domaincoursemapperservice.mapper;

import com.github.karixdev.commonservice.model.schedule.raw.TimeCell;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class TimeCellMapperTest {
    TimeCellMapper underTest = new TimeCellMapper();

    @Test
    void GivenNotEmptyTimeCellsSet_WhenGetScheduleStartTime_ThenReturnsProperStartTime() {
        // Given
        TimeCell timeCell = new TimeCell("10:00-11:00");

        // When
        LocalTime result = underTest.mapToLocalTime(timeCell);

        // Then
        assertThat(result).isEqualTo(LocalTime.of(10, 0));
    }
}