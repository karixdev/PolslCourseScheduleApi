package com.github.karixdev.scheduleservice.controller;

import com.github.karixdev.commonservice.event.EventType;
import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import com.github.karixdev.scheduleservice.ContainersEnvironment;
import com.github.karixdev.scheduleservice.entity.Schedule;
import com.github.karixdev.scheduleservice.repository.ScheduleRepository;
import com.github.karixdev.scheduleservice.utils.KeycloakUtils;
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
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ScheduleControllerIT extends ContainersEnvironment {

    @Autowired
    WebTestClient webClient;

    @Autowired
    ScheduleRepository scheduleRepository;

    Consumer<String, ScheduleEvent> scheduleEventConsumer;

    private static final String SCHEDULE_EVENT_TOPIC = "schedule.event";

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(kafkaContainer.getBootstrapServers(), "schedule-domain-test-consumer", "true");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        ConsumerFactory<String, ScheduleEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        scheduleEventConsumer = consumerFactory.createConsumer();
        scheduleEventConsumer.subscribe(List.of(SCHEDULE_EVENT_TOPIC));
    }

    @AfterEach
    void tearDown() {
        scheduleRepository.deleteAll();
        scheduleEventConsumer.close();
    }

    @Test
    void shouldNotCreateScheduleForStandardUser() {
        String token = KeycloakUtils.getUserToken(keycloakContainer.getAuthServerUrl());

        String payload = """
                {
                    "type": 1,
                    "planPolslId": 1999,
                    "semester": 2,
                    "name": "schedule-name",
                    "groupNumber": 1,
                    "wd": 0
                }
                """;

        webClient.post().uri("/api/schedules")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isForbidden();

        assertThat(scheduleRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotCreateSchedule() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule-name")
                .groupNumber(1)
                .wd(0)
                .build());

        String payload = """
                {
                    "type": 1,
                    "planPolslId": 1999,
                    "semester": 2,
                    "name": "schedule-name",
                    "groupNumber": 1,
                    "wd": 0
                }
                """;

        webClient.post().uri("/api/schedules")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        assertThat(scheduleRepository.findAll())
                .hasSize(1);
    }

    @Test
    void shouldCreateSchedule() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        String payload = """
                {
                    "type": 1,
                    "planPolslId": 1999,
                    "semester": 2,
                    "name": "available-name",
                    "groupNumber": 1,
                    "wd": 0
                }
                """;

        webClient.post().uri("/api/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.semester").isEqualTo(2)
                .jsonPath("$.name").isEqualTo("available-name")
                .jsonPath("$.groupNumber").isEqualTo(1);

        List<Schedule> schedules = scheduleRepository.findAll();
        ConsumerRecord<String, ScheduleEvent> consumerRecord =
                KafkaTestUtils.getSingleRecord(scheduleEventConsumer, SCHEDULE_EVENT_TOPIC, Duration.ofSeconds(20));

        assertThat(schedules)
                .hasSize(1);

        Schedule schedule = schedules.get(0);

        assertThat(schedule.getType())
                .isEqualTo(1);
        assertThat(schedule.getPlanPolslId())
                .isEqualTo(1999);
        assertThat(schedule.getSemester())
                .isEqualTo(2);
        assertThat(schedule.getName())
                .isEqualTo("available-name");
        assertThat(schedule.getGroupNumber())
                .isEqualTo(1);

        ScheduleEvent expectedEvent = ScheduleEvent.builder()
                .eventType(EventType.CREATE)
                .scheduleId(schedule.getId().toString())
                .type(schedule.getType())
                .planPolslId(schedule.getPlanPolslId())
                .wd(schedule.getWd())
                .build();

        assertThat(consumerRecord.key()).isEqualTo(schedule.getId().toString());
        assertThat(consumerRecord.value()).isEqualTo(expectedEvent);
    }

    @Test
    void shouldGetAllCourses() {
        scheduleRepository.saveAll(List.of(
                Schedule.builder()
                        .type(1)
                        .planPolslId(2000)
                        .semester(1)
                        .name("schedule1")
                        .groupNumber(2)
                        .wd(1)
                        .build(),
                Schedule.builder()
                        .type(1)
                        .planPolslId(1999)
                        .semester(1)
                        .name("schedule2")
                        .groupNumber(1)
                        .wd(4)
                        .build(),
                Schedule.builder()
                        .type(1)
                        .planPolslId(1000)
                        .semester(2)
                        .name("schedule3")
                        .groupNumber(1)
                        .wd(0)
                        .build()
        ));

        webClient.get().uri("/api/schedules")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.*").isArray()
                .jsonPath("$[0].semester").isEqualTo(1)
                .jsonPath("$[0].groupNumber").isEqualTo(1)
                .jsonPath("$[1].semester").isEqualTo(1)
                .jsonPath("$[1].groupNumber").isEqualTo(2)
                .jsonPath("$[2].semester").isEqualTo(2)
                .jsonPath("$[2].groupNumber").isEqualTo(1);
    }

    @Test
    void shouldRetrieveAllSchedulesWithProvidedIdsInCorrectOrder() {
        Schedule schedule1 = scheduleRepository.save(
                Schedule.builder()
                        .type(1)
                        .planPolslId(2000)
                        .semester(1)
                        .name("schedule1")
                        .groupNumber(2)
                        .wd(1)
                        .build()
        );

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(4)
                .build());

        Schedule schedule3 = scheduleRepository.save(
                Schedule.builder()
                        .type(1)
                        .planPolslId(1000)
                        .semester(2)
                        .name("schedule3")
                        .groupNumber(1)
                        .wd(0)
                        .build()
        );

        String ids = String.join(",", List.of(
                schedule1.getId().toString(),
                schedule3.getId().toString()));

        webClient.get().uri("/api/schedules?ids=" + ids)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.*").isArray()
                .jsonPath("$[0].id").isEqualTo(
                        schedule1.getId().toString())
                .jsonPath("$[1].id").isEqualTo(
                        schedule3.getId().toString());
    }

    @Test
    void shouldNotFindScheduleById() {
        String id = UUID.randomUUID().toString();

        webClient.get().uri("/api/schedules/" + id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo(
                        String.format(
                                "Schedule with id %s not found",
                                id
                        ));
    }

    @Test
    void shouldFindScheduleById() {
        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(2000)
                .semester(1)
                .name("schedule1")
                .groupNumber(2)
                .wd(2)
                .build());

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(0)
                .build());

        webClient.get().uri("/api/schedules/" + schedule.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(schedule.getId().toString())
                .jsonPath("$.semester").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("schedule1")
                .jsonPath("$.groupNumber").isEqualTo(2);
    }

    @Test
    void shouldNotUpdateScheduleForStandardUser() {
        String token = KeycloakUtils.getUserToken(keycloakContainer.getAuthServerUrl());
        UUID id = UUID.randomUUID();

        String payload = """
                {
                    "type": 1,
                    "planPolslId": 1999,
                    "semester": 2,
                    "name": "schedule-name",
                    "groupNumber": 1,
                    "wd": 0
                }
                """;

        webClient.put().uri("/api/schedules/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldNotUpdateNotExistingSchedule() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());
        UUID id = UUID.randomUUID();

        String payload = """
                {
                    "type": 1,
                    "planPolslId": 1999,
                    "semester": 2,
                    "name": "schedule-name",
                    "groupNumber": 1,
                    "wd": 0
                }
                """;

        webClient.put().uri("/api/schedules/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldNotUpdateScheduleWhileTryingToAssignUnavailableName() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(2000)
                .semester(1)
                .name("schedule1")
                .groupNumber(2)
                .wd(4)
                .build());

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(0)
                .build());

        String payload = """
                {
                    "type": 1,
                    "planPolslId": 1999,
                    "semester": 2,
                    "name": "schedule2",
                    "groupNumber": 1
                }
                """;

        webClient.put().uri("/api/schedules/" + schedule.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldUpdateSchedule() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(2000)
                .semester(1)
                .name("schedule1")
                .groupNumber(2)
                .wd(0)
                .build());

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(0)
                .build());

        String payload = """
                {
                    "type": 2,
                    "planPolslId": 1999,
                    "semester": 5,
                    "name": "schedule3",
                    "groupNumber": 7,
                    "wd": 0
                }
                """;

        webClient.put().uri("/api/schedules/" + schedule.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(schedule.getId().toString())
                .jsonPath("$.semester").isEqualTo(5)
                .jsonPath("$.name").isEqualTo("schedule3")
                .jsonPath("$.groupNumber").isEqualTo(7);

        Optional<Schedule> optionalSchedule =
                scheduleRepository.findById(schedule.getId());
        ConsumerRecord<String, ScheduleEvent> consumerRecord =
                KafkaTestUtils.getSingleRecord(scheduleEventConsumer, SCHEDULE_EVENT_TOPIC, Duration.ofSeconds(20));

        assertThat(optionalSchedule).isNotEmpty();

        schedule = optionalSchedule.get();

        ScheduleEvent expectedEvent = ScheduleEvent.builder()
                .eventType(EventType.UPDATE)
                .scheduleId(schedule.getId().toString())
                .type(schedule.getType())
                .planPolslId(schedule.getPlanPolslId())
                .wd(schedule.getWd())
                .build();

        assertThat(consumerRecord.key()).isEqualTo(schedule.getId().toString());
        assertThat(consumerRecord.value()).isEqualTo(expectedEvent);
    }

    @Test
    void shouldNotDeleteScheduleForStandardUser() {
        String token = KeycloakUtils.getUserToken(keycloakContainer.getAuthServerUrl());

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(2)
                .build());

        webClient.delete().uri("/api/schedules/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldNotDeleteNotExistingSchedule() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(2)
                .build());

        webClient.delete().uri("/api/schedules/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldDeleteSchedule() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(2)
                .build());

        webClient.delete().uri("/api/schedules/" + schedule.getId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        ConsumerRecord<String, ScheduleEvent> consumerRecord =
                KafkaTestUtils.getSingleRecord(scheduleEventConsumer, SCHEDULE_EVENT_TOPIC, Duration.ofSeconds(20));

        assertThat(scheduleRepository.count()).isZero();

        ScheduleEvent expectedEvent = ScheduleEvent.builder()
                .eventType(EventType.DELETE)
                .scheduleId(schedule.getId().toString())
                .build();

        assertThat(consumerRecord.key()).isEqualTo(schedule.getId().toString());
        assertThat(consumerRecord.value()).isEqualTo(expectedEvent);
    }

    @Test
    void shouldNotRequestScheduleCoursesUpdateForNotExistingSchedule() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(2)
                .build());

        webClient.post().uri("/api/schedules/" + UUID.randomUUID() + "/courses/update")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldRequestScheduleCourses() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(2)
                .build());

        webClient.post().uri("/api/schedules/" + schedule.getId() + "/courses/update")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        ConsumerRecord<String, ScheduleEvent> consumerRecord =
                KafkaTestUtils.getSingleRecord(scheduleEventConsumer, SCHEDULE_EVENT_TOPIC, Duration.ofSeconds(20));

        ScheduleEvent expectedEvent = ScheduleEvent.builder()
                .eventType(EventType.UPDATE)
                .scheduleId(schedule.getId().toString())
                .type(schedule.getType())
                .planPolslId(schedule.getPlanPolslId())
                .wd(schedule.getWd())
                .build();

        assertThat(consumerRecord.key()).isEqualTo(schedule.getId().toString());
        assertThat(consumerRecord.value()).isEqualTo(expectedEvent);
    }

}
