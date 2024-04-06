package com.github.karixdev.courseservice.application.validator;

import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawCourse;
import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawCourseType;
import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawCourseWeekType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessedRawCourseValidatorTest {

    ProcessedRawCourseValidator underTest = new ProcessedRawCourseValidator();

    @ParameterizedTest
    @MethodSource("invalidProcessedRawCourses")
    void GivenInvalidProcessedRawCourse_WhenValidate_ThenReturnsFalse(ProcessedRawCourse input) {
        // When
        boolean result = underTest.isValid(input);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void GivenValidProcessedRawCourse_WhenValidate_ThenReturnsTrue() {
        // Given
        ProcessedRawCourse processedRawCourse = ProcessedRawCourse.builder()
                .courseType(ProcessedRawCourseType.LAB)
                .weekType(ProcessedRawCourseWeekType.EVEN)
                .name("Physics")
                .scheduleId(UUID.randomUUID())
                .teachers("dr. Smith")
                .classrooms("410")
                .dayOfWeek(DayOfWeek.MONDAY)
                .startsAt(LocalTime.of(9, 21))
                .endsAt(LocalTime.of(12, 32))
                .build();

        // When
        boolean result = underTest.isValid(processedRawCourse);

        // Then
        assertThat(result).isTrue();
    }

    private static Stream<Arguments> invalidProcessedRawCourses() {
        return Stream.of(
                Arguments.of(
                        ProcessedRawCourse.builder()
                                .courseType(ProcessedRawCourseType.LAB)
                                .weekType(ProcessedRawCourseWeekType.EVEN)
                                .name("Physics")
                                .scheduleId(null)
                                .teachers("dr. Smith")
                                .classrooms("410")
                                .dayOfWeek(DayOfWeek.MONDAY)
                                .startsAt(LocalTime.of(9, 21))
                                .endsAt(LocalTime.of(12, 32))
                                .build()
                ),
                Arguments.of(
                        ProcessedRawCourse.builder()
                                .courseType(ProcessedRawCourseType.LAB)
                                .weekType(ProcessedRawCourseWeekType.EVEN)
                                .name(null)
                                .scheduleId(UUID.randomUUID())
                                .teachers("dr. Smith")
                                .classrooms("410")
                                .dayOfWeek(DayOfWeek.MONDAY)
                                .startsAt(LocalTime.of(9, 21))
                                .endsAt(LocalTime.of(12, 32))
                                .build()
                ),
                Arguments.of(
                        ProcessedRawCourse.builder()
                                .courseType(null)
                                .weekType(ProcessedRawCourseWeekType.EVEN)
                                .name("Physics")
                                .scheduleId(UUID.randomUUID())
                                .teachers("dr. Smith")
                                .classrooms("410")
                                .dayOfWeek(DayOfWeek.MONDAY)
                                .startsAt(LocalTime.of(9, 21))
                                .endsAt(LocalTime.of(12, 32))
                                .build()
                ),
                Arguments.of(
                        ProcessedRawCourse.builder()
                                .courseType(ProcessedRawCourseType.LAB)
                                .weekType(ProcessedRawCourseWeekType.EVEN)
                                .name("Physics")
                                .scheduleId(UUID.randomUUID())
                                .teachers("dr. Smith")
                                .classrooms("410")
                                .dayOfWeek(null)
                                .startsAt(LocalTime.of(9, 21))
                                .endsAt(LocalTime.of(12, 32))
                                .build()
                ),
                Arguments.of(
                        ProcessedRawCourse.builder()
                                .courseType(ProcessedRawCourseType.LAB)
                                .weekType(ProcessedRawCourseWeekType.EVEN)
                                .name("Physics")
                                .scheduleId(UUID.randomUUID())
                                .teachers("dr. Smith")
                                .classrooms("410")
                                .dayOfWeek(DayOfWeek.MONDAY)
                                .startsAt(null)
                                .endsAt(LocalTime.of(12, 32))
                                .build()
                ),
                Arguments.of(
                        ProcessedRawCourse.builder()
                                .courseType(ProcessedRawCourseType.LAB)
                                .weekType(ProcessedRawCourseWeekType.EVEN)
                                .name("Physics")
                                .scheduleId(UUID.randomUUID())
                                .teachers("dr. Smith")
                                .classrooms("410")
                                .dayOfWeek(DayOfWeek.MONDAY)
                                .startsAt(LocalTime.of(9, 21))
                                .endsAt(null)
                                .build()
                ),
                Arguments.of(
                        ProcessedRawCourse.builder()
                                .courseType(ProcessedRawCourseType.LAB)
                                .weekType(null)
                                .name("Physics")
                                .scheduleId(UUID.randomUUID())
                                .teachers("dr. Smith")
                                .classrooms("410")
                                .dayOfWeek(DayOfWeek.MONDAY)
                                .startsAt(LocalTime.of(9, 21))
                                .endsAt(LocalTime.of(12, 32))
                                .build()
                ),
                Arguments.of(
                        ProcessedRawCourse.builder()
                                .courseType(null)
                                .weekType(null)
                                .name(null)
                                .scheduleId(null)
                                .teachers("dr. Smith")
                                .classrooms("410")
                                .dayOfWeek(null)
                                .startsAt(null)
                                .endsAt(null)
                                .build()
                )
        );
    }

}