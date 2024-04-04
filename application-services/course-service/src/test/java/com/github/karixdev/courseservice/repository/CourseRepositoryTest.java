package com.github.karixdev.courseservice.repository;

import com.github.karixdev.courseservice.ContainersEnvironment;
import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.entity.CourseType;
import com.github.karixdev.courseservice.entity.WeekType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CourseRepositoryTest extends ContainersEnvironment {

    @Autowired
    CourseRepository underTest;

    @Autowired
    TestEntityManager em;

    @Test
    void GivenScheduleId_WhenFindByScheduleId_ThenReturnsCorrectList() {
        // Given
        UUID scheduleId = UUID.randomUUID();

        Course course1 = em.persist(Course.builder()
                .scheduleId(scheduleId)
                .name("course-name")
                .courseType(CourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build());

        Course course2 = em.persist(Course.builder()
                .scheduleId(scheduleId)
                .name("course-name-2")
                .courseType(CourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build());

        em.persist(Course.builder()
                .scheduleId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .name("course-name-3")
                .courseType(CourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build());

        // When
        List<Course> result = underTest.findByScheduleId(scheduleId);

        // Then
        assertThat(result).containsExactly(course1, course2);
    }
}