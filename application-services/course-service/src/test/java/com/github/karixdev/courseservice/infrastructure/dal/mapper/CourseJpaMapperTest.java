package com.github.karixdev.courseservice.infrastructure.dal.mapper;

import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntity;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntityCourseType;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntityWeekType;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CourseJpaMapperTest {

    CourseJpaMapper underTest = new CourseJpaMapper();

    @Test
    void GivenDomainEntity_WhenMapToJpa_ThenReturnsCorrectJpaEntity() {
        // Given
        Course course = Course.builder()
                .scheduleId(UUID.randomUUID())
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
        CourseEntity result = underTest.toJpa(course);

        // Then
        CourseEntity expected = CourseEntity.builder()
                .scheduleId(course.getScheduleId())
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .courseType(CourseEntityCourseType.LAB)
                .teachers("dr Adam, dr Marcin")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVERY)
                .classrooms("LAB 1")
                .additionalInfo("Only on 8.03")
                .build();

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void GivenJpaEntity_WhenMapToDomain_ThenReturnsCorrectDomainEntity() {
        // Given
        CourseEntity course = CourseEntity.builder()
                .scheduleId(UUID.randomUUID())
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .courseType(CourseEntityCourseType.LAB)
                .teachers("dr Adam, dr Marcin")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVERY)
                .classrooms("LAB 1")
                .additionalInfo("Only on 8.03")
                .build();

        // When
        Course result = underTest.toDomain(course);

        // Then
        Course expected = Course.builder()
                .scheduleId(course.getScheduleId())
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