package com.github.karixdev.domainmodelmapperservice.application.mapper;

import com.github.karixdev.domainmodelmapperservice.domain.processed.CourseType;
import com.github.karixdev.domainmodelmapperservice.domain.processed.ProcessedRawCourse;
import com.github.karixdev.domainmodelmapperservice.domain.processed.WeekType;
import com.github.karixdev.domainmodelmapperservice.domain.raw.RawAnchor;
import com.github.karixdev.domainmodelmapperservice.domain.raw.RawCourse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessedRawCourseMapperTest {

    ProcessedRawCourseMapper underTest = new ProcessedRawCourseMapper();

    LocalTime startTime;

    UUID scheduleId;

    @BeforeEach
    void setUp() {
        startTime = LocalTime.of(8, 0);
        scheduleId = UUID.randomUUID();
    }

    @Test
    void GivenValidPlanPolslResponse_WhenMap_ThenReturnsValidSetOfCourses() {
        // Given
        RawCourse courseCell = RawCourse.builder()
                .top(259)
                .left(254)
                .height(135)
                .width(154)
                .text("course 1")
                .build();

        // When
        ProcessedRawCourse result = underTest.map(courseCell, scheduleId, startTime);

        // Then
        assertThat(result.startsAt())
                .isEqualTo(LocalTime.of(8, 30));
        assertThat(result.endsAt())
                .isEqualTo(LocalTime.of(11, 45));
    }

    @Test
    void GivenCourseCellWithLinks_WhenMap_ThenReturnsValidCourse() {
        // Given
        RawCourse courseCell = RawCourse.builder()
                .top(259)
                .left(254)
                .height(135)
                .width(154)
                .anchors(Set.of(
                        RawAnchor.builder()
                                .text("teacher 1")
                                .address("plan.php?id=10&type=10")
                                .build(),
                        RawAnchor.builder()
                                .text("teacher 2")
                                .address("plan.php?id=10&type=10")
                                .build(),
                        RawAnchor.builder()
                                .text("room 1")
                                .address("plan.php?id=10&type=20")
                                .build(),
                        RawAnchor.builder()
                                .text("room 2")
                                .address("plan.php?id=10&type=20")
                                .build(),
                        RawAnchor.builder()
                                .text("other link 2")
                                .address("plan.php?id=10")
                                .build()
                ))
                .text("text")
                .build();

        // When
        ProcessedRawCourse result = underTest.map(courseCell, scheduleId, startTime);

        // Then
        assertThat(result.teachers())
                .isIn("teacher 1, teacher 2", "teacher 2, teacher 1");
        assertThat(result.classroom())
                .isIn("room 1, room 2", "teacher 2, teacher 1");
    }

    @ParameterizedTest
    @MethodSource("courseTypesInputParameters")
    void GivenCourseCellWithNameContainingType_WhenMap_ThenReturnsCoursesWithValidCourseType(String name, CourseType expectedType) {
        // Given
        RawCourse courseCell = RawCourse.builder()
                .top(259)
                .left(254)
                .height(135)
                .width(154)
                .text(name)
                .build();

        // When
        ProcessedRawCourse result = underTest.map(courseCell, scheduleId, startTime);

        // Then
        assertThat(result.courseType())
                .isEqualTo(expectedType);
    }

    @ParameterizedTest
    @MethodSource("courseDayOfWeekLeftValues")
    void GivenCourseWithDifferentCourseLeftValues_WhenMap_ThenReturnsCourseWithProperDayOfWeek(int left, DayOfWeek expectedDay) {
        // Given
        RawCourse courseCell = RawCourse.builder()
                .top(259)
                .left(left)
                .height(135)
                .width(154)
                .text("course")
                .build();

        // When
        ProcessedRawCourse result = underTest.map(courseCell, scheduleId, startTime);

        // Then
        assertThat(result.dayOfWeek())
                .isEqualTo(expectedDay);
    }

    @ParameterizedTest
    @MethodSource("courseWeeksLeftAndCwValues")
    void GivenCourseWithDifferentLeftAndCwValues_WhenMap_ThenReturnsCourseWithCorrectWeeks(int left, int width, WeekType expectedWeeks) {
        // Given
        RawCourse courseCell = RawCourse.builder()
                .top(259)
                .left(left)
                .width(width)
                .text("course")
                .height(0)
                .build();

        // When
        ProcessedRawCourse result = underTest.map(courseCell, scheduleId, startTime);

        // Then
        assertThat(result.weekType())
                .isEqualTo(expectedWeeks);
    }

    @Test
    void GivenCourseCellWithAdditionalInfo_WhenMap_ThenReturnsCourseWithProperAdditionalInfo() {
        // Given
        RawCourse courseCell = RawCourse.builder()
                .top(259)
                .left(254)
                .height(135)
                .width(154)
                .text("""
                        course 1, ćw
                        występowanie:
                        1.03
                        """)
                .build();

        // When
        ProcessedRawCourse result = underTest.map(courseCell, scheduleId, startTime);

        // Then
        assertThat(result.additionalInfo())
                .isEqualTo("występowanie: 1.03");
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
                Arguments.of(88, 71, WeekType.ODD),
                Arguments.of(171, 71, WeekType.EVEN),
                // tuesday
                Arguments.of(254, 71, WeekType.ODD),
                Arguments.of(337, 71, WeekType.EVEN),
                // wednesday
                Arguments.of(420, 71, WeekType.ODD),
                Arguments.of(503, 71, WeekType.EVEN),
                // thursday
                Arguments.of(586, 71, WeekType.ODD),
                Arguments.of(669, 71, WeekType.EVEN),
                // friday
                Arguments.of(752, 71, WeekType.ODD),
                Arguments.of(835, 71, WeekType.EVEN),

                Arguments.of(88, 154, WeekType.EVERY),
                Arguments.of(254, 154, WeekType.EVERY),
                Arguments.of(420, 154, WeekType.EVERY),
                Arguments.of(586, 154, WeekType.EVERY),
                Arguments.of(752, 154, WeekType.EVERY)
        );
    }

}
