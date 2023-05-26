package com.github.karixdev.courseservice.controller;

import com.github.karixdev.courseservice.ContainersEnvironment;
import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.entity.CourseType;
import com.github.karixdev.courseservice.entity.WeekType;
import com.github.karixdev.courseservice.repository.CourseRepository;
import com.github.karixdev.courseservice.testconfig.WebClientTestConfig;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = {WebClientTestConfig.class})
@WireMockTest(httpPort = 9999)
public class CourseControllerIT extends ContainersEnvironment {
    @Autowired
    WebTestClient webClient;

    @Autowired
    CourseRepository courseRepository;

    @DynamicPropertySource
    static void overrideScheduleServiceBaseUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "schedule-service.base-url",
                () -> "http://localhost:9999");
    }

    @AfterEach
    void tearDown() {
        courseRepository.deleteAll();
    }

    @Test
    void shouldNotCreateCourseForNotExistingSchedule() {
        String token = getAdminToken();

        stubFor(
                get(urlPathEqualTo("/api/schedules/11111111-1111-1111-1111-111111111111"))
                        .willReturn(aResponse().withStatus(404))
        );

        String payload = """
                {
                    "schedule_id": "11111111-1111-1111-1111-111111111111",
                    "starts_at": "08:30",
                    "ends_at": "10:15",
                    "name": "course-name",
                    "course_type": "LAB",
                    "teachers": "dr Adam",
                    "day_of_week": "FRIDAY",
                    "week_type": "EVEN",
                    "classrooms": "LAB 1",
                    "additional_info": "Only on 3.08"
                }
                """;

        webClient.post().uri("/api/courses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        assertThat(courseRepository.findAll()).isEmpty();
    }

    @Test
    void shouldCreateCourse() {
        String token = getAdminToken();

        stubFor(
                get(urlPathEqualTo("/api/schedules/11111111-1111-1111-1111-111111111111"))
                        .willReturn(ok()
                                .withHeader(
                                        "Content-Type",
                                        "application/json"
                                )
                                .withBody("""
                                        {
                                            "id": "11111111-1111-1111-1111-111111111111"
                                        }
                                        """
                                )
                        )
        );

        String payload = """
                {
                    "schedule_id": "11111111-1111-1111-1111-111111111111",
                    "starts_at": "08:30",
                    "ends_at": "10:15",
                    "name": "course-name",
                    "course_type": "LAB",
                    "teachers": "dr Adam",
                    "day_of_week": "FRIDAY",
                    "week_type": "EVEN",
                    "classrooms": "LAB 1",
                    "additional_info": "Only on 3.08"
                }
                """;

        webClient.post().uri("/api/courses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated();

        List<Course> courses = courseRepository.findAll();

        assertThat(courses).hasSize(1);

        Course course = courses.get(0);

        assertThat(course.getScheduleId()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(course.getName()).isEqualTo("course-name");
        assertThat(course.getCourseType()).isEqualTo(CourseType.LAB);
        assertThat(course.getTeachers()).isEqualTo("dr Adam");
        assertThat(course.getClassroom()).isEqualTo("LAB 1");
        assertThat(course.getAdditionalInfo()).isEqualTo("Only on 3.08");
        assertThat(course.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(course.getWeekType()).isEqualTo(WeekType.EVEN);
        assertThat(course.getStartsAt()).isEqualTo(LocalTime.of(8, 30));
        assertThat(course.getEndsAt()).isEqualTo(LocalTime.of(10, 15));
    }

    @Test
    void shouldNotUpdateNotExistingCourse() {
        String token = getAdminToken();

        String payload = """
                {
                    "schedule_id": "11111111-1111-1111-1111-111111111111",
                    "starts_at": "08:30",
                    "ends_at": "10:15",
                    "name": "course-name",
                    "course_type": "LAB",
                    "teachers": "dr Adam",
                    "day_of_week": "FRIDAY",
                    "week_type": "EVEN",
                    "classrooms": "LAB 1",
                    "additional_info": "Only on 3.08"
                }
                """;

        webClient.put().uri("/api/courses/11111111-1111-1111-1111-111111111112")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNotFound();

        assertThat(courseRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotUpdateCourseForNotExistingSchedule() {
        String token = getAdminToken();

        stubFor(
                get(urlPathEqualTo("/api/schedules/11111111-1111-1111-1111-111111111111"))
                        .willReturn(aResponse().withStatus(404))
        );

        Course course = courseRepository.save(Course.builder()
                .scheduleId(UUID.randomUUID())
                .name("course-name")
                .courseType(CourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build());

        String payload = """
                {
                    "schedule_id": "11111111-1111-1111-1111-111111111111",
                    "starts_at": "08:30",
                    "ends_at": "10:15",
                    "name": "course-name",
                    "course_type": "LAB",
                    "teachers": "dr Adam",
                    "day_of_week": "FRIDAY",
                    "week_type": "EVEN",
                    "classrooms": "LAB 1",
                    "additional_info": "Only on 3.08"
                }
                """;

        webClient.put().uri("/api/courses/" + course.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        List<Course> results = courseRepository.findAll();

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(course);
    }

    @Test
    void shouldUpdateCourse() {
        String token = getAdminToken();

        Course course = courseRepository.save(Course.builder()
                .scheduleId(UUID.randomUUID())
                .name("course-name")
                .courseType(CourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build());

        stubFor(
                get(urlPathEqualTo("/api/schedules/11111111-1111-1111-1111-111111111111"))
                        .willReturn(ok()
                                .withHeader(
                                        "Content-Type",
                                        "application/json"
                                )
                                .withBody("""
                                        {
                                            "id": "11111111-1111-1111-1111-111111111111"
                                        }
                                        """
                                )
                        )
        );

        String payload = """
                {
                    "schedule_id": "11111111-1111-1111-1111-111111111111",
                    "starts_at": "10:30",
                    "ends_at": "12:15",
                    "name": "course-name-2",
                    "course_type": "LECTURE",
                    "teachers": "dr Marcin",
                    "day_of_week": "MONDAY",
                    "week_type": "EVERY",
                    "classrooms": "LAB 3",
                    "additional_info": null
                }
                """;

        webClient.put().uri("/api/courses/" + course.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk();

        List<Course> results = courseRepository.findAll();

        assertThat(results).hasSize(1);

        Course resultCourse = results.get(0);

        assertThat(resultCourse.getScheduleId()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(resultCourse.getName()).isEqualTo("course-name-2");
        assertThat(resultCourse.getCourseType()).isEqualTo(CourseType.LECTURE);
        assertThat(resultCourse.getTeachers()).isEqualTo("dr Marcin");
        assertThat(resultCourse.getClassroom()).isEqualTo("LAB 3");
        assertThat(resultCourse.getAdditionalInfo()).isNull();
        assertThat(resultCourse.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(resultCourse.getWeekType()).isEqualTo(WeekType.EVERY);
        assertThat(resultCourse.getStartsAt()).isEqualTo(LocalTime.of(10, 30));
        assertThat(resultCourse.getEndsAt()).isEqualTo(LocalTime.of(12, 15));
    }
}