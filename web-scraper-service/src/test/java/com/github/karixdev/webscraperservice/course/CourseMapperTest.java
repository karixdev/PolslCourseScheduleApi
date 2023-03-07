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

import java.time.DayOfWeek;
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
                                "course 1"
                        ),
                        new CourseCell(
                                417,
                                254,
                                56,
                                154,
                                "course 2"
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
                        DayOfWeek.TUESDAY

                ),
                new Course(
                        LocalTime.of(12, 0),
                        LocalTime.of(13, 30),
                        "course 2",
                        CourseType.INFO,
                        DayOfWeek.TUESDAY
                )
        );

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void GivenPlanPolslResponseWithCourseLinks_WhenMap_ThenReturnsValidSetOfCourses() {
        // Given
        PlanPolslResponse planPolslResponse = new PlanPolslResponse(
                Set.of(
                        new TimeCell("08:00-09:00")
                ),
                Set.of(
                        new CourseCell(
                                259,
                                254,
                                135,
                                154,
                                "course",
                                Set.of(
                                        new Link("teacher", "plan.php?id=10&type=10"),
                                        new Link("other link", "plan.php?id=10&type=20"),
                                        new Link("other link 2", "plan.php?id=10")

                                )
                        )
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
                        CourseType.INFO,
                        Set.of("teacher"),
                        DayOfWeek.TUESDAY
                )
        );

        assertThat(courses).isEqualTo(expected);
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
                        expectedType,
                        DayOfWeek.TUESDAY
                )
        );

        assertThat(courses).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("courseDayOfWeekLeftValues")
    void GivenPlanPolslResponseWithDifferentCourseLeftValues_WhenMap_ThenReturnsValidSetOfCourses(int left, DayOfWeek expectedDay) {
        // Given
        PlanPolslResponse planPolslResponse = new PlanPolslResponse(
                Set.of(
                        new TimeCell("08:00-09:00"),
                        new TimeCell("09:00-10:00")
                ),
                Set.of(
                        new CourseCell(259, left, 135, 154, "course")
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
                        CourseType.INFO,
                        expectedDay
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

    private static Stream<Arguments> courseDayOfWeekLeftValues() {
        return Stream.of(
                Arguments.of(88, DayOfWeek.MONDAY),
                Arguments.of(254, DayOfWeek.TUESDAY),
                Arguments.of(420, DayOfWeek.WEDNESDAY),
                Arguments.of(586, DayOfWeek.THURSDAY),
                Arguments.of(752, DayOfWeek.FRIDAY)
        );
    }
}
