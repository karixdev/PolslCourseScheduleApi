package com.github.karixdev.courseservice.controller;

import com.github.karixdev.commonservice.event.course.ScheduleCoursesEvent;
import com.github.karixdev.courseservice.ContainersEnvironment;
import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.entity.CourseType;
import com.github.karixdev.courseservice.entity.WeekType;
import com.github.karixdev.courseservice.repository.CourseRepository;
import com.github.karixdev.courseservice.testconfig.TestKafkaTopicsConfig;
import com.github.karixdev.courseservice.testconfig.WebClientTestConfig;
import com.github.karixdev.courseservice.utils.KeycloakUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = {WebClientTestConfig.class, TestKafkaTopicsConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CourseControllerIT extends ContainersEnvironment {

    @Autowired
    WebTestClient webClient;

    @Autowired
    CourseRepository courseRepository;

    WireMockServer wm;

    Consumer<String, ScheduleCoursesEvent> scheduleCoursesEventConsumer;
    private static final String SCHEDULE_COURSES_EVENT_TOPIC = "schedule.courses.event";

    @DynamicPropertySource
    static void overrideScheduleServiceBaseUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "schedule-service.base-url",
                () -> "http://localhost:9999");
    }

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                kafkaContainer.getBootstrapServers(),
                "course-service-schedule-courses-event-test",
                "true"
        );
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        ConsumerFactory<String, ScheduleCoursesEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        scheduleCoursesEventConsumer = consumerFactory.createConsumer();
        scheduleCoursesEventConsumer.subscribe(List.of(SCHEDULE_COURSES_EVENT_TOPIC));

        wm = new WireMockServer(9999);
        wm.start();
    }

    @AfterEach
    void tearDown() {
        wm.stop();
        scheduleCoursesEventConsumer.close();
        courseRepository.deleteAll();
    }

    @Test
    void shouldNotCreateForUser() {
        String token = KeycloakUtils.getUserToken(keycloakContainer.getAuthServerUrl());;

        wm.stubFor(
                get(urlPathEqualTo("/api/schedules/11111111-1111-1111-1111-111111111111"))
                        .willReturn(aResponse().withStatus(404))
        );

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

        webClient.post().uri("/api/courses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isForbidden();

        assertThat(courseRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotCreateCourseForNotExistingSchedule() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());;

        wm.stubFor(
                get(urlPathEqualTo("/api/schedules/11111111-1111-1111-1111-111111111111"))
                        .willReturn(aResponse().withStatus(404))
        );

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
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());;

        wm.stubFor(
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

        webClient.post().uri("/api/courses")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated();

        ConsumerRecord<String, ScheduleCoursesEvent> consumerRecord =
                KafkaTestUtils.getSingleRecord(scheduleCoursesEventConsumer, SCHEDULE_COURSES_EVENT_TOPIC, Duration.ofSeconds(20));
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

        assertThat(consumerRecord.value().created()).containsExactly(course.getId().toString());
    }

    @Test
    void shouldNotUpdateForUser() {
        String token = KeycloakUtils.getUserToken(keycloakContainer.getAuthServerUrl());;

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

        webClient.put().uri("/api/courses/11111111-1111-1111-1111-111111111112")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isForbidden();

        assertThat(courseRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotUpdateNotExistingCourse() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());;

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
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());;

        wm.stubFor(
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
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());;

        Course course = courseRepository.save(Course.builder()
                .scheduleId(UUID.randomUUID())
                .name("course-name")
                .courseType(CourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build());

        wm.stubFor(
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
                    "scheduleId": "11111111-1111-1111-1111-111111111111",
                    "startsAt": "10:30",
                    "endsAt": "12:15",
                    "name": "course-name-2",
                    "courseType": "LECTURE",
                    "teachers": "dr Marcin",
                    "dayOfWeek": "MONDAY",
                    "weekType": "EVERY",
                    "classrooms": "LAB 3",
                    "additionalInfo": null
                }
                """;

        webClient.put().uri("/api/courses/" + course.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk();

        ConsumerRecord<String, ScheduleCoursesEvent> consumerRecord =
                KafkaTestUtils.getSingleRecord(scheduleCoursesEventConsumer, SCHEDULE_COURSES_EVENT_TOPIC, Duration.ofSeconds(20));
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

        assertThat(consumerRecord.value().updated()).containsExactly(resultCourse.getId().toString());
    }

    @Test
    void shouldNotDeleteCourseForUser() {
        String token = KeycloakUtils.getUserToken(keycloakContainer.getAuthServerUrl());;

        webClient.delete().uri("/api/courses/11111111-1111-1111-1111-111111111111")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldNotDeleteNotExistingCourse() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());;

        webClient.delete().uri("/api/courses/11111111-1111-1111-1111-111111111111")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldDeleteCourse() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());;

        Course course = Course.builder()
                .scheduleId(UUID.randomUUID())
                .name("course-name")
                .courseType(CourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        courseRepository.save(course);

        webClient.delete().uri("/api/courses/" + course.getId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        ConsumerRecord<String, ScheduleCoursesEvent> consumerRecord =
                KafkaTestUtils.getSingleRecord(scheduleCoursesEventConsumer, SCHEDULE_COURSES_EVENT_TOPIC, Duration.ofSeconds(20));

        assertThat(courseRepository.findAll()).isEmpty();
        assertThat(consumerRecord.value().deleted()).containsExactly(course.getId().toString());
    }

    @Test
    void shouldRetrieveAllScheduleCoursesInCorrectOrder() {
        UUID scheduleId = UUID.randomUUID();

        Course course1 = Course.builder()
                .scheduleId(scheduleId)
                .name("course-name-1")
                .courseType(CourseType.INFO)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        Course course2 = Course.builder()
                .scheduleId(scheduleId)
                .name("course-name-2")
                .courseType(CourseType.LAB)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(10, 30))
                .endsAt(LocalTime.of(12, 15))
                .build();

        Course course3 = Course.builder()
                .scheduleId(scheduleId)
                .name("course-name-3")
                .courseType(CourseType.LECTURE)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        Course course4 = Course.builder()
                .scheduleId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .name("course-name-4")
                .courseType(CourseType.LECTURE)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .weekType(WeekType.EVERY)
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(10, 15))
                .build();

        courseRepository.saveAll(List.of(course1, course2, course3, course4));

        webClient.get().uri("/api/courses/schedule/" + scheduleId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(course3.getId().toString())
                .jsonPath("$[1].id").isEqualTo(course2.getId().toString())
                .jsonPath("$[2].id").isEqualTo(course1.getId().toString());
    }
}