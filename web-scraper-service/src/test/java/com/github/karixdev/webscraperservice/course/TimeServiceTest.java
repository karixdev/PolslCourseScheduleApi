package com.github.karixdev.webscraperservice.course;

import com.github.karixdev.webscraperservice.course.exception.NoScheduleStartTimeException;
import com.github.karixdev.webscraperservice.planpolsl.domain.TimeCell;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TimeServiceTest {
    TimeService underTest = new TimeService();

    @Test
    void GivenEmptyTimeCellsSet_WhenGetScheduleStartTime_ThenThrowsNoScheduleStartTimeException() {
        // Given
        Set<TimeCell> timeCells = Set.of();

        // When & Then
        assertThatThrownBy(() -> underTest.getScheduleStartTime(timeCells))
                .isInstanceOf(NoScheduleStartTimeException.class)
                .hasMessage("Could not find schedule start time");
    }

    @Test
    void GivenNotEmptyTimeCellsSet_WhenGetScheduleStartTime_ThenReturnsProperStartTime() {
        // Given
        Set<TimeCell> timeCells = Set.of(
                new TimeCell("09:00-10:00"),
                new TimeCell("11:00-12:00"),
                new TimeCell("08:00-09:00"),
                new TimeCell("10:00-11:00")
        );

        // When
        LocalTime result = underTest.getScheduleStartTime(timeCells);

        // Then
        assertThat(result).isEqualTo(LocalTime.of(8, 0));
    }
}
