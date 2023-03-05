package com.github.karixdev.webscraperservice.course;

import com.github.karixdev.webscraperservice.course.domain.Course;
import com.github.karixdev.webscraperservice.course.domain.CourseType;
import com.github.karixdev.webscraperservice.course.exception.NoScheduleStartTimeException;
import com.github.karixdev.webscraperservice.planpolsl.domain.CourseCell;
import com.github.karixdev.webscraperservice.planpolsl.domain.Link;
import com.github.karixdev.webscraperservice.planpolsl.domain.PlanPolslResponse;
import com.github.karixdev.webscraperservice.planpolsl.domain.TimeCell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Stream;

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
    void GivenValidPlanPolslResponse_WhenMap_ThenReturnsValidSetOfCourses() {
        // Given
        PlanPolslResponse planPolslResponse = new PlanPolslResponse(
                Set.of(
                        new TimeCell("08:00-09:00"),
                        new TimeCell("09:00-10:00")
                ),
                Set.of(
                        new CourseCell(
                                259,
                                254,
                                135,
                                154,
                                "course 1",
                                Set.of(
                                        new Link("dr. Big-head",
                                                "plan.php?type=10&id=100"),
                                        new Link("dr. Bigger-head",
                                                "plan.php?type=10&id=123")
                                )
                        ),
                        new CourseCell(
                                417,
                                254,
                                56,
                                154,
                                "course 2",
                                Set.of(
                                        new Link(
                                                "room 2",
                                                "plan.php?id=100")
                                )
                        )
                )
        );

        // When
        Set<Course> result = underTest.map(planPolslResponse);

        // Then
        Set<Course> expected = Set.of(
                new Course(
                        LocalTime.of(8, 30),
                        LocalTime.of(11, 45),
                        "course 1",
                        CourseType.INFO,
                        Set.of("dr. Big-head", "dr. Bigger-head")
                ),
                new Course(
                        LocalTime.of(12, 0),
                        LocalTime.of(13, 30),
                        "course 2",
                        CourseType.INFO
                )
        );

        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("courseTypesInputParameters")
    void GivenPlanPolslResponseWithDifferentCourseTypes_WhenMap_ThenReturnsValidSetOfCourses(String name, CourseType expectedType) {
        // Given
        PlanPolslResponse planPolslResponse = new PlanPolslResponse(
                Set.of(
                        new TimeCell("08:00-09:00"),
                        new TimeCell("09:00-10:00")
                ),
                Set.of(
                        new CourseCell(259, 254, 135, 154, name)
                )
        );

        // When
        Set<Course> courses = underTest.map(planPolslResponse);

        // Then
        Set<Course> expected = Set.of(
                new Course(
                        LocalTime.of(8, 30),
                        LocalTime.of(11, 45),
                        "course",
                        expectedType
                )
        );

        assertThat(courses).isEqualTo(expected);
    }

    private static Stream<Arguments> courseTypesInputParameters() {
        return Stream.of(
                Arguments.of("course, wyk", CourseType.LECTURE),
                Arguments.of("course, lab", CourseType.LAB),
                Arguments.of("course, proj", CourseType.PROJECT),
                Arguments.of("course, ćw", CourseType.PRACTICAL),
                Arguments.of("course", CourseType.INFO)
        );
    }
}
