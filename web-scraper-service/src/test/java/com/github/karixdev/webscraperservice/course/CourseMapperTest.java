package com.github.karixdev.webscraperservice.course;

import com.github.karixdev.webscraperservice.course.domain.Course;
import com.github.karixdev.webscraperservice.course.domain.CourseType;
import com.github.karixdev.webscraperservice.course.domain.Weeks;
import com.github.karixdev.webscraperservice.planpolsl.domain.CourseCell;
import com.github.karixdev.webscraperservice.planpolsl.domain.Link;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class CourseMapperTest {
    CourseMapper underTest = new CourseMapper();

    LocalTime startTime;

    @BeforeEach
    void setUp() {
        startTime = LocalTime.of(8, 0);
    }

    @Test
    void GivenValidPlanPolslResponse_WhenMap_ThenReturnsValidSetOfCourses() {
        // Given
        CourseCell courseCell = new CourseCell(
                259,
                254,
                135,
                154,
                "course 1"
        );

        // When
        Course result = underTest.map(courseCell, startTime);

        // Then
        assertThat(result.startsAt())
                .isEqualTo(LocalTime.of(8, 30));
        assertThat(result.endsAt())
                .isEqualTo(LocalTime.of(11, 45));
    }

    @Test
    void GivenCourseCellWithLinks_WhenMap_ThenReturnsValidCourse() {
        // Given
        CourseCell courseCell = new CourseCell(
                259,
                254,
                135,
                154,
                "course",
                Set.of(
                        new Link("teacher", "plan.php?id=10&type=10"),
                        new Link("room", "plan.php?id=10&type=20"),
                        new Link("other link 2", "plan.php?id=10")

                )
        );

        // When
        Course result = underTest.map(courseCell, startTime);

        // Then
        assertThat(result.teachers())
                .isEqualTo(Set.of("teacher"));
        assertThat(result.rooms())
                .isEqualTo(Set.of("room"));
    }

    @ParameterizedTest
    @MethodSource("courseTypesInputParameters")
    void GivenCourseCellWithNameContainingType_WhenMap_ThenReturnsCoursesWithValidCourseType(String name, CourseType expectedType) {
        // Given
        CourseCell courseCell = new CourseCell(259, 254, 135, 154, name);

        // When
        Course result = underTest.map(courseCell, startTime);

        // Then
        assertThat(result.courseType())
                .isEqualTo(expectedType);
    }

    @ParameterizedTest
    @MethodSource("courseDayOfWeekLeftValues")
    void GivenCourseWithDifferentCourseLeftValues_WhenMap_ThenReturnsCourseWithProperDayOfWeek(int left, DayOfWeek expectedDay) {
        // Given
        CourseCell courseCell = new CourseCell(259, left, 135, 154, "course");

        // When
        Course result = underTest.map(courseCell, startTime);

        // Then
        assertThat(result.dayOfWeek())
                .isEqualTo(expectedDay);
    }

    @ParameterizedTest
    @MethodSource("courseWeeksLeftAndCwValues")
    void GivenCourseWithDifferentLeftAndCwValues_WhenMap_ThenReturnsCourseWithCorrectWeeks(int left, int cw, Weeks expectedWeeks) {
        // Given
        CourseCell courseCell = new CourseCell(259, left, 135, cw, "course");

        // When
        Course result = underTest.map(courseCell, startTime);

        // Then
        assertThat(result.weeks())
                .isEqualTo(expectedWeeks);
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

    private static Stream<Arguments> courseWeeksLeftAndCwValues() {
        return Stream.of(
                // monday
                Arguments.of(88, 71, Weeks.ODD),
                Arguments.of(171, 71, Weeks.EVEN),
                // tuesday
                Arguments.of(254, 71, Weeks.ODD),
                Arguments.of(337, 71, Weeks.EVEN),
                // wednesday
                Arguments.of(420, 71, Weeks.ODD),
                Arguments.of(503, 71, Weeks.EVEN),
                // thursday
                Arguments.of(586, 71, Weeks.ODD),
                Arguments.of(669, 71, Weeks.EVEN),
                // friday
                Arguments.of(752, 71, Weeks.ODD),
                Arguments.of(835, 71, Weeks.EVEN),

                Arguments.of(88, 154, Weeks.EVERY),
                Arguments.of(254, 154, Weeks.EVERY),
                Arguments.of(420, 154, Weeks.EVERY),
                Arguments.of(586, 154, Weeks.EVERY),
                Arguments.of(752, 154, Weeks.EVERY)
        );
    }
}
