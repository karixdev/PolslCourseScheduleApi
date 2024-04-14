package com.github.karixdev.courseservice.infrastructure.rest.controller.admin;

import com.github.karixdev.courseservice.ContainersEnvironment;
import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntity;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntityCourseType;
import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntityWeekType;
import com.github.karixdev.courseservice.infrastructure.dal.repository.CourseEntityRepository;
import com.github.karixdev.courseservice.testconfig.TestKafkaTopicsConfig;
import com.github.karixdev.courseservice.testconfig.WebClientTestConfig;
import com.github.karixdev.courseservice.utils.KeycloakUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
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
class CourseAdminControllerIT extends ContainersEnvironment {


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

}