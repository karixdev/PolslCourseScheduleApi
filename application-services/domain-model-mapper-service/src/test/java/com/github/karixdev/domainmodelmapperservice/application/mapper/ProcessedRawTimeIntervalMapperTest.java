package com.github.karixdev.domainmodelmapperservice.application.mapper;

import com.github.karixdev.domainmodelmapperservice.domain.processed.ProcessedRawTimeInterval;
import com.github.karixdev.domainmodelmapperservice.domain.raw.RawTimeInterval;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessedRawTimeIntervalMapperTest {
    ProcessedRawTimeIntervalMapper underTest = new ProcessedRawTimeIntervalMapper();

    @Test
    void GivenNotEmptyTimeCellsSet_WhenGetScheduleStartTime_ThenReturnsProperStartTime() {
        // Given
        RawTimeInterval timeCell = new RawTimeInterval("10:00", "11:00");

        // When
        ProcessedRawTimeInterval result = underTest.map(timeCell);

        // Then
        ProcessedRawTimeInterval expected = ProcessedRawTimeInterval.builder()
                .start(LocalTime.of(10, 0))
                .end(LocalTime.of(11, 0))
                .build();

        assertThat(result).isEqualTo(expected);
    }
}