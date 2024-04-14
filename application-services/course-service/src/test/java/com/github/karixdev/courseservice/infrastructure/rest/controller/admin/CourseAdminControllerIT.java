package com.github.karixdev.courseservice.infrastructure.rest.controller.admin;

import com.github.karixdev.courseservice.RestControllerITContainersEnvironment;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntity;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntityCourseType;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntityWeekType;
import com.github.karixdev.courseservice.infrastructure.dal.repository.CourseEntityRepository;
import com.github.karixdev.courseservice.testconfig.TestKafkaTopicsConfig;
import com.github.karixdev.courseservice.testconfig.WebClientTestConfig;
import com.github.karixdev.courseservice.utils.KeycloakUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = {WebClientTestConfig.class, TestKafkaTopicsConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CourseAdminControllerIT extends RestControllerITContainersEnvironment {


    @Autowired
    WebTestClient webClient;

    @Autowired
    CourseEntityRepository courseRepository;

    WireMockServer wm;

    @DynamicPropertySource
    static void overrideScheduleServiceBaseUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "schedule-service.base-url",
                () -> "http://localhost:9999");
    }

    @BeforeEach
    void setUp() {
        wm = new WireMockServer(9999);
        wm.start();
    }

    @AfterEach
    void tearDown() {
        wm.stop();
        courseRepository.deleteAll();
    }

    @Test
    void shouldNotAllowStandardUserToCreateCourse() {
        String token = KeycloakUtils.getUserToken(keycloakContainer.getAuthServerUrl());

        String payload = """
                {
                    "scheduleId": "11111111-1111-1111-1111-111111111111",
                    "startsAt": "08:30",
                    "endsAt": "10:15",
                    "name": "course-name",
                    "courseType": "LAB",
                    "teachers": "dr Adam",
                    "dayOfWeek": "FRIDAY",
                    "weekType": "EVEN",
                    "classrooms": "LAB 1",
                    "additionalInfo": "Only on 3.08"
                }
                """;

        webClient.post().uri("/api/admin/courses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isForbidden();

        assertThat(courseRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotCreateCourseWithIdOfNotExistingSchedule() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        UUID scheduleId = UUID.randomUUID();

        String payload = """
                {
                    "scheduleId": "%s",
                    "startsAt": "08:30",
                    "endsAt": "10:15",
                    "name": "course-name",
                    "courseType": "LAB",
                    "teachers": "dr Adam",
                    "dayOfWeek": "FRIDAY",
                    "weekType": "EVEN",
                    "classrooms": "LAB 1",
                    "additionalInfo": "Only on 3.08"
                }
                """.formatted(scheduleId);

        wm.stubFor(
                get(urlPathEqualTo("/api/queries/schedules/%s".formatted(scheduleId)))
                        .willReturn(aResponse().withStatus(404))
        );

        webClient.post().uri("/api/admin/courses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        assertThat(courseRepository.findAll()).isEmpty();
    }

    @Test
    void shouldCreateCourse() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        UUID scheduleId = UUID.randomUUID();

        String payload = """
                {
                    "scheduleId": "%s",
                    "startsAt": "08:30",
                    "endsAt": "10:15",
                    "name": "course-name",
                    "courseType": "LAB",
                    "teachers": "dr Adam",
                    "dayOfWeek": "FRIDAY",
                    "weekType": "EVEN",
                    "classrooms": "LAB 1",
                    "additionalInfo": "Only on 3.08"
                }
                """.formatted(scheduleId);

        wm.stubFor(
                get(urlPathEqualTo("/api/queries/schedules/%s".formatted(scheduleId)))
                        .willReturn(ok()
                                .withHeader(
                                        "Content-Type",
                                        "application/json"
                                )
                                .withBody("""
                                        {
                                            "id": "%s"
                                        }
                                        """.formatted(scheduleId)
                                )
                        )
        );

        webClient.post().uri("/api/admin/courses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNoContent();

        List<CourseEntity> allCourses = courseRepository.findAll();
        CourseEntity expected = CourseEntity.builder()
                .name("course-name")
                .scheduleId(scheduleId)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .courseType(CourseEntityCourseType.LAB)
                .teachers("dr Adam")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVEN)
                .classrooms("LAB 1")
                .additionalInfo("Only on 3.08")
                .build();

        assertThat(allCourses).hasSize(1);
        assertThat(allCourses.get(0))
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);
    }

    @Test
    void shouldNotAllowUserToUpdateSchedule() {
        String token = KeycloakUtils.getUserToken(keycloakContainer.getAuthServerUrl());

        CourseEntity course = CourseEntity.builder()
                .id(UUID.randomUUID())
                .name("course-name")
                .scheduleId(UUID.randomUUID())
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .courseType(CourseEntityCourseType.LAB)
                .teachers("dr Adam")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVEN)
                .classrooms("LAB 1")
                .additionalInfo("Only on 3.08")
                .build();

        String payload = """
                {
                    "scheduleId": "11111111-1111-1111-1111-111111111111",
                    "startsAt": "08:30",
                    "endsAt": "10:15",
                    "name": "course-name",
                    "courseType": "LAB",
                    "teachers": "dr Adam",
                    "dayOfWeek": "FRIDAY",
                    "weekType": "EVEN",
                    "classrooms": "LAB 1",
                    "additionalInfo": "Only on 3.08"
                }
                """;

        courseRepository.save(course);

        webClient.put().uri("/api/admin/courses/%s".formatted(course.getId()))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isForbidden();

        List<CourseEntity> allCourses = courseRepository.findAll();

        assertThat(allCourses).hasSize(1);
        AssertionsForClassTypes.assertThat(allCourses.get(0))
                .usingRecursiveComparison()
                .isEqualTo(course);
    }

    @Test
    void shouldNotUpdateNotExistingSchedule() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        CourseEntity course = CourseEntity.builder()
                .id(UUID.randomUUID())
                .name("course-name")
                .scheduleId(UUID.randomUUID())
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .courseType(CourseEntityCourseType.LAB)
                .teachers("dr Adam")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVEN)
                .classrooms("LAB 1")
                .additionalInfo("Only on 3.08")
                .build();

        String payload = """
                {
                    "scheduleId": "11111111-1111-1111-1111-111111111111",
                    "startsAt": "08:30",
                    "endsAt": "10:15",
                    "name": "course-name",
                    "courseType": "LAB",
                    "teachers": "dr Adam",
                    "dayOfWeek": "FRIDAY",
                    "weekType": "EVEN",
                    "classrooms": "LAB 1",
                    "additionalInfo": "Only on 3.08"
                }
                """;

        courseRepository.save(course);

        webClient.put().uri("/api/admin/courses/%s".formatted(UUID.randomUUID()))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNotFound();

        List<CourseEntity> allCourses = courseRepository.findAll();

        assertThat(allCourses).hasSize(1);
        AssertionsForClassTypes.assertThat(allCourses.get(0))
                .usingRecursiveComparison()
                .isEqualTo(course);
    }

    @Test
    void shouldNotUpdateCourseWithIdOfNotExistingSchedule() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        UUID newScheduleId = UUID.randomUUID();

        CourseEntity course = CourseEntity.builder()
                .id(UUID.randomUUID())
                .name("course-name")
                .scheduleId(UUID.randomUUID())
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .courseType(CourseEntityCourseType.LAB)
                .teachers("dr Adam")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVEN)
                .classrooms("LAB 1")
                .additionalInfo("Only on 3.08")
                .build();

        String payload = """
                {
                    "scheduleId": "%s",
                    "startsAt": "08:30",
                    "endsAt": "10:15",
                    "name": "course-name",
                    "courseType": "LAB",
                    "teachers": "dr Adam",
                    "dayOfWeek": "FRIDAY",
                    "weekType": "EVEN",
                    "classrooms": "LAB 1",
                    "additionalInfo": "Only on 3.08"
                }
                """.formatted(newScheduleId);

        courseRepository.save(course);

        wm.stubFor(
                get(urlPathEqualTo("/api/queries/schedules/%s".formatted(newScheduleId)))
                        .willReturn(aResponse().withStatus(404))
        );

        webClient.put().uri("/api/admin/courses/%s".formatted(course.getId()))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        List<CourseEntity> allCourses = courseRepository.findAll();

        assertThat(allCourses).hasSize(1);
        AssertionsForClassTypes.assertThat(allCourses.get(0))
                .usingRecursiveComparison()
                .isEqualTo(course);
    }

    @Test
    void shouldUpdateCourse() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        UUID newScheduleId = UUID.randomUUID();

        CourseEntity course = CourseEntity.builder()
                .id(UUID.randomUUID())
                .scheduleId(UUID.randomUUID())
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .courseType(CourseEntityCourseType.LAB)
                .teachers("dr Adam")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVEN)
                .classrooms("LAB 1")
                .additionalInfo("Only on 3.08")
                .build();

        String payload = """
                {
                    "scheduleId": "%s",
                    "startsAt": "09:45",
                    "endsAt": "12:20",
                    "name": "new-course-name",
                    "courseType": "LECTURE",
                    "teachers": "dr Marcin",
                    "dayOfWeek": "MONDAY",
                    "weekType": "EVERY",
                    "classrooms": "3a",
                    "additionalInfo": "Bring notes"
                }
                """.formatted(newScheduleId);

        courseRepository.save(course);

        wm.stubFor(
                get(urlPathEqualTo("/api/queries/schedules/%s".formatted(newScheduleId)))
                        .willReturn(ok()
                                .withHeader(
                                        "Content-Type",
                                        "application/json"
                                )
                                .withBody("""
                                        {
                                            "id": "%s"
                                        }
                                        """.formatted(newScheduleId)
                                )
                        )
        );

        webClient.put().uri("/api/admin/courses/%s".formatted(course.getId()))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNoContent();

        List<CourseEntity> allCourses = courseRepository.findAll();

        CourseEntity expected = CourseEntity.builder()
                .id(course.getId())
                .scheduleId(newScheduleId)
                .startsAt(LocalTime.of(9, 45))
                .endsAt(LocalTime.of(12, 20))
                .name("new-course-name")
                .courseType(CourseEntityCourseType.LECTURE)
                .teachers("dr Marcin")
                .dayOfWeek(DayOfWeek.MONDAY)
                .weekType(CourseEntityWeekType.EVERY)
                .classrooms("3a")
                .additionalInfo("Bring notes")
                .build();

        assertThat(allCourses).hasSize(1);
        AssertionsForClassTypes.assertThat(allCourses.get(0))
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldNotAllowUserToDeleteSchedule() {
        String token = KeycloakUtils.getUserToken(keycloakContainer.getAuthServerUrl());

        CourseEntity course = CourseEntity.builder()
                .id(UUID.randomUUID())
                .scheduleId(UUID.randomUUID())
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .courseType(CourseEntityCourseType.LAB)
                .teachers("dr Adam")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVEN)
                .classrooms("LAB 1")
                .additionalInfo("Only on 3.08")
                .build();

        courseRepository.save(course);

        webClient.delete().uri("/api/admin/courses/%s".formatted(course.getId()))
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();

        assertThat(courseRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldNotDeleteNotExistingSchedule() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        CourseEntity course = CourseEntity.builder()
                .id(UUID.randomUUID())
                .scheduleId(UUID.randomUUID())
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .courseType(CourseEntityCourseType.LAB)
                .teachers("dr Adam")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVEN)
                .classrooms("LAB 1")
                .additionalInfo("Only on 3.08")
                .build();

        courseRepository.save(course);

        webClient.delete().uri("/api/admin/courses/%s".formatted(UUID.randomUUID()))
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();

        assertThat(courseRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldDeleteSchedule() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        CourseEntity course1 = CourseEntity.builder()
                .id(UUID.randomUUID())
                .scheduleId(UUID.randomUUID())
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .name("course-name")
                .courseType(CourseEntityCourseType.LAB)
                .teachers("dr Adam")
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(CourseEntityWeekType.EVEN)
                .classrooms("LAB 1")
                .additionalInfo("Only on 3.08")
                .build();

        CourseEntity course2 = CourseEntity.builder()
                .id(UUID.randomUUID())
                .scheduleId(UUID.randomUUID())
                .startsAt(LocalTime.of(9, 31))
                .endsAt(LocalTime.of(15, 25))
                .name("course-name-2")
                .courseType(CourseEntityCourseType.LECTURE)
                .teachers("dr Phil")
                .dayOfWeek(DayOfWeek.MONDAY)
                .weekType(CourseEntityWeekType.ODD)
                .classrooms("LAB 2")
                .additionalInfo("Only on 4.08")
                .build();

        courseRepository.saveAll(List.of(course1, course2));

        webClient.delete().uri("/api/admin/courses/%s".formatted(course1.getId()))
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        List<CourseEntity> allCourses = courseRepository.findAll();

        assertThat(allCourses).hasSize(1);
        assertThat(allCourses.get(0)).isEqualTo(course2);
    }

}