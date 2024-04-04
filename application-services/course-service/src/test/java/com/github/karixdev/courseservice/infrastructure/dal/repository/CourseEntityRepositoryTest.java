package com.github.karixdev.courseservice.infrastructure.dal.repository;

import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntity;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntityCourseType;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntityWeekType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CourseEntityRepositoryTest {

    @Autowired
    CourseEntityRepository underTest;

    @Autowired
    TestEntityManager em;

    static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15.1-alpine")
                    .withUsername("root")
                    .withPassword("root")
                    .withDatabaseName("course-service-test");

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
    }

    @Test
    void GivenScheduleId_WhenDeleteScheduleById_ThenDeletesCorrectCourses() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        em.persist(CourseEntity.builder()
                .scheduleId(scheduleId)
                .name("course-name")
                .courseType(CourseEntityCourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build());

        em.persist(CourseEntity.builder()
                .scheduleId(scheduleId)
                .name("course-name-2")
                .courseType(CourseEntityCourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build());

        CourseEntity course1 = em.persist(CourseEntity.builder()
                .scheduleId(UUID.randomUUID())
                .name("course-name-3")
                .courseType(CourseEntityCourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build());

        em.flush();

        // When
        underTest.deleteByScheduleId(scheduleId);

        // Then
        assertThat(underTest.findAll()).containsExactly(course1);
    }

    @Test
    void GivenScheduleId_WhenFindByScheduleId_ThenReturnsListWithProperCourses() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        CourseEntity course1 = em.persist(CourseEntity.builder()
                .scheduleId(scheduleId)
                .name("course-name")
                .courseType(CourseEntityCourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build());

        CourseEntity course2 = em.persist(CourseEntity.builder()
                .scheduleId(scheduleId)
                .name("course-name-2")
                .courseType(CourseEntityCourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build());

        em.persist(CourseEntity.builder()
                .scheduleId(UUID.randomUUID())
                .name("course-name-3")
                .courseType(CourseEntityCourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build());

        em.flush();

        // When
        List<CourseEntity> result = underTest.findByScheduleId(scheduleId);

        // Then
        assertThat(result).containsExactly(course1, course2);
    }

    @DynamicPropertySource
    static void overrideDatabaseConnectionProperties(DynamicPropertyRegistry registry) {
        if (!postgreSQLContainer.isCreated()) {
            return;
        }

        registry.add(
                "spring.datasource.url",
                postgreSQLContainer::getJdbcUrl);

        registry.add(
                "spring.datasource.username",
                postgreSQLContainer::getUsername);

        registry.add(
                "spring.datasource.password",
                postgreSQLContainer::getPassword);

        registry.add(
                "spring.datasource.driver-class-name",
                postgreSQLContainer::getDriverClassName);
    }

}