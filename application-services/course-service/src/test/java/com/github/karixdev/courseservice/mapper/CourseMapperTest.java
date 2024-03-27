package com.github.karixdev.courseservice.mapper;

import com.github.karixdev.courseservice.dto.CourseResponse;
import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.entity.CourseType;
import com.github.karixdev.courseservice.entity.WeekType;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CourseMapperTest {

    CourseMapper underTest = new CourseMapper();

    @Test
    void GivenCourse_WhenMap_ThenReturnsCorrectCourseResponse() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        Course course = Course.builder()
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("Calculus")
                .courseType(CourseType.LAB)
                .teachers("dr Adam, mgr Marcin")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVEN)
                .classroom("314 RMS, CEK Room C")
                .additionalInfo(null)
                .scheduleId(scheduleId)
                .build();

        // When
        CourseResponse result = underTest.map(course);

        // Then
        assertThat(result.getName())
                .isEqualTo("Calculus");

        assertThat(result.getCourseType())
                .isEqualTo(CourseType.LAB);

        assertThat(result.getAdditionalInfo())
                .isNull();

        assertThat(result.getDayOfWeek())
                .isEqualTo(DayOfWeek.FRIDAY);

        assertThat(result.getStartsAt())
                .isEqualTo(LocalTime.of(8, 30));

        assertThat(result.getEndsAt())
                .isEqualTo(LocalTime.of(10, 15));

        assertThat(result.getTeachers())
                .isIn("dr Adam, mgr Marcin", "mgr Marcin, dr Adam");

        assertThat(result.getClassrooms())
                .isIn("314 RMS, CEK Room C", "CEK Room C, 314 RMS");

        assertThat(result.getScheduleId())
                .isEqualTo(scheduleId);

        assertThat(result.getWeekType())
                .isEqualTo(WeekType.EVEN);
    }
}
