package com.github.karixdev.webscraperservice.course;

import com.github.karixdev.webscraperservice.course.domain.Course;
import com.github.karixdev.webscraperservice.course.exception.NoScheduleStartTimeException;
import com.github.karixdev.webscraperservice.planpolsl.domain.CourseCell;
import com.github.karixdev.webscraperservice.planpolsl.domain.PlanPolslResponse;
import com.github.karixdev.webscraperservice.planpolsl.domain.TimeCell;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CourseMapperTest {
    CourseMapper underTest = new CourseMapper();

    @Test
    void GivenPlanPolslResponseWithEmptyTimeCellsSet_WhenMap_ThenThrowsNoScheduleStartTimeException() {
        // Given
        PlanPolslResponse planPolslResponse = new PlanPolslResponse(
                Set.of(),
                Set.of()
        );

        // When & Then
        assertThatThrownBy(() -> underTest.map(planPolslResponse))
                .isInstanceOf(NoScheduleStartTimeException.class)
                .hasMessage("Could not find schedule start time");
    }

    @Test
    void GivenValidPlanPolsl_WhenMap_ThenReturnsValidSetOfCourses() {
        // Given
        PlanPolslResponse planPolslResponse = new PlanPolslResponse(
                Set.of(
                        new TimeCell("08:00-09:00"),
                        new TimeCell("09:00-10:00")
                ),
                Set.of(
                        new CourseCell(259, 254, 135, 154, "course 1"),
                        new CourseCell(417, 254, 56, 154, "course 2")
                )
        );

        // When
        Set<Course> result = underTest.map(planPolslResponse);

        // Then
        Set<Course> expected = Set.of(
                new Course(
                        LocalTime.of(8, 30),
                        LocalTime.of(11, 45)
                ),
                new Course(
                        LocalTime.of(12, 0),
                        LocalTime.of(13, 30)
                )
        );

        assertThat(result).isEqualTo(expected);
    }
}
