package com.github.karixdev.courseservice.infrastructure.rest.controller.user;

import com.github.karixdev.courseservice.RestControllerITContainersEnvironment;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntity;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntityCourseType;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntityWeekType;
import com.github.karixdev.courseservice.infrastructure.dal.repository.CourseEntityRepository;
import com.github.karixdev.courseservice.testconfig.TestKafkaTopicsConfig;
import com.github.karixdev.courseservice.testconfig.WebClientTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = {WebClientTestConfig.class, TestKafkaTopicsConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CourseControllerIT extends RestControllerITContainersEnvironment {

    @Autowired
    WebTestClient webClient;

    @Autowired
    CourseEntityRepository courseRepository;

    @AfterEach
    void tearDown() {
        courseRepository.deleteAll();
    }

    @Test
    void shouldRetrieveCoursesInChronologicalOrder() {
        UUID scheduleId = UUID.randomUUID();

        CourseEntity course3 = CourseEntity.builder()
                .id(UUID.randomUUID())
                .scheduleId(scheduleId)
                .name("course-3")
                .teachers("teacher-3")
                .courseType(CourseEntityCourseType.INFO)
                .classrooms("classroom-3")
                .additionalInfo("additional-info-3")
                .dayOfWeek(DayOfWeek.THURSDAY)
                .weekType(CourseEntityWeekType.EVEN)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 0))
                .build();

        CourseEntity course1 = CourseEntity.builder()
                .id(UUID.randomUUID())
                .scheduleId(scheduleId)
                .name("course-1")
                .teachers("teacher-1")
                .courseType(CourseEntityCourseType.LECTURE)
                .classrooms("classroom-1")
                .additionalInfo("additional-info-1")
                .dayOfWeek(DayOfWeek.MONDAY)
                .weekType(CourseEntityWeekType.EVERY)
                .startsAt(LocalTime.of(10, 15))
                .endsAt(LocalTime.of(11, 45))
                .build();

        CourseEntity course2 = CourseEntity.builder()
                .id(UUID.randomUUID())
                .scheduleId(scheduleId)
                .name("course-2")
                .teachers("teacher-2")
                .courseType(CourseEntityCourseType.LECTURE)
                .classrooms("classroom-2")
                .additionalInfo("additional-info-2")
                .dayOfWeek(DayOfWeek.MONDAY)
                .weekType(CourseEntityWeekType.EVEN)
                .startsAt(LocalTime.of(12, 0))
                .endsAt(LocalTime.of(13, 30))
                .build();

        CourseEntity course4 = CourseEntity.builder()
                .id(UUID.randomUUID())
                .scheduleId(scheduleId)
                .name("course-4")
                .teachers("teacher-4")
                .courseType(CourseEntityCourseType.INFO)
                .classrooms("classroom-4")
                .additionalInfo("additional-info-4")
                .dayOfWeek(DayOfWeek.THURSDAY)
                .weekType(CourseEntityWeekType.EVEN)
                .startsAt(LocalTime.of(13, 30))
                .endsAt(LocalTime.of(15, 0))
                .build();

        courseRepository.saveAll(List.of(course3, course2, course1, course4));

        courseRepository.saveAll(
                IntStream.range(0, 2)
                        .mapToObj(i -> CourseEntity.builder()
                                .id(UUID.randomUUID())
                                .scheduleId(UUID.randomUUID())
                                .name("course-" + i)
                                .courseType(CourseEntityCourseType.LAB)
                                .teachers("teacher-" + i)
                                .classrooms("classroom-" + i)
                                .additionalInfo("additional-info-" + i)
                                .dayOfWeek(DayOfWeek.MONDAY)
                                .weekType(CourseEntityWeekType.EVEN)
                                .startsAt(LocalTime.of(i, 10))
                                .endsAt(LocalTime.of(10, i))
                                .build())
                        .toList()
        );

        webClient.get().uri("/api/courses/schedule/%s".formatted(scheduleId))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.[0].id").isEqualTo(course1.getId().toString())
                .jsonPath("$.[1].id").isEqualTo(course2.getId().toString())
                .jsonPath("$.[2].id").isEqualTo(course3.getId().toString())
                .jsonPath("$.[3].id").isEqualTo(course4.getId().toString());
    }

}