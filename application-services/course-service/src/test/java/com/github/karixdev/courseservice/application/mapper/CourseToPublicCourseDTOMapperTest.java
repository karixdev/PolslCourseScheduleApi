package com.github.karixdev.courseservice.application.mapper;

import com.github.karixdev.courseservice.application.dto.user.PublicCourseDTO;
import com.github.karixdev.courseservice.application.dto.user.PublicCourseTypeDTO;
import com.github.karixdev.courseservice.application.dto.user.PublicCourseWeekTypeDTO;
import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CourseToPublicCourseDTOMapperTest {

    CourseToPublicCourseDTOMapper underTest = new CourseToPublicCourseDTOMapper();

    @Test
    void GivenCourse_WhenMap_ThenReturnsCorrectDTO() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        Course course = Course.builder()
                .scheduleId(scheduleId)
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

        // When
        PublicCourseDTO result = underTest.map(course);

        // Then
        PublicCourseDTO expected = PublicCourseDTO.builder()
                .scheduleId(scheduleId)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .courseType(PublicCourseTypeDTO.LAB)
                .teachers("dr Adam, dr Marcin")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(PublicCourseWeekTypeDTO.EVERY)
                .classrooms("LAB 1")
                .additionalInfo("Only on 8.03")
                .build();

        assertThat(result).isEqualTo(expected);
    }

}