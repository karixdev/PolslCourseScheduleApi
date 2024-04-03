package com.github.karixdev.courseservice.application.mapper;

import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawCourse;
import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawCourseType;
import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawCourseWeekType;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessedRawCourseToCourseMapperTest {

    ProcessedRawCourseToCourseMapper underTest = new ProcessedRawCourseToCourseMapper();

    @Test
    void GivenProcessedRawCourse_WhenMap_ThenReturnsCorrectCourse() {
        // Given
        ProcessedRawCourse processedRawCourse = ProcessedRawCourse.builder()
                .scheduleId(UUID.randomUUID())
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .courseType(ProcessedRawCourseType.LAB)
                .teachers("dr Adam, dr Marcin")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(ProcessedRawCourseWeekType.EVERY)
                .classrooms("LAB 1")
                .additionalInfo("Only on 8.03")
                .build();

        // When
        Course result = underTest.map(processedRawCourse);

        //Then
        Course expected = Course.builder()
                .scheduleId(processedRawCourse.scheduleId())
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .courseType(CourseType.LAB)
                .teachers("dr Adam, dr Marcin")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .classrooms("LAB 1")
                .additionalInfo("Only on 8.03")
                .build();

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

}