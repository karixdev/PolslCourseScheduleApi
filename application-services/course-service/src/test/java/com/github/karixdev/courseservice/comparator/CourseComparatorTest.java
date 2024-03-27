package com.github.karixdev.courseservice.comparator;

import com.github.karixdev.courseservice.entity.Course;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class CourseComparatorTest {

    CourseComparator underTest = new CourseComparator();

    @Test
    void GivenCoursesTakingPlaceInDifferentDays_WhenCompare_ThenReturnsCorrectValue() {
        // Given
        Course course1 = Course.builder()
                .dayOfWeek(DayOfWeek.FRIDAY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        Course course2 = Course.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        // When
        int result = underTest.compare(course1, course2);

        // Then
        assertThat(result).isPositive();
    }

    @Test
    void GivenCoursesTakingPlaceInTheSameDayAtDifferentTime_WhenCompare_ThenReturnsCorrectValue() {
        // Given
        Course course1 = Course.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        Course course2 = Course.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startsAt(LocalTime.of(11, 27))
                .endsAt(LocalTime.of(14, 15))
                .build();

        // When
        int result = underTest.compare(course1, course2);

        // Then
        assertThat(result).isNegative();
    }

    @Test
    void GivenCoursesTakingPlaceInTheSameDayAndAtTheSameTime_WhenCompare_ThenReturnsCorrectValue() {
        // Given
        Course course1 = Course.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        Course course2 = Course.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(14, 15))
                .build();

        // When
        int result = underTest.compare(course1, course2);

        // Then
        assertThat(result).isZero();
    }
}
