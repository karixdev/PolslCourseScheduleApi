package com.github.karixdev.scheduleservice.infrastructure.rest.controller;

import com.github.karixdev.scheduleservice.ContainersEnvironment;
import com.github.karixdev.scheduleservice.application.event.EventType;
import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import com.github.karixdev.scheduleservice.infrastructure.dal.repository.JpaScheduleRepository;
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
class ScheduleCommandControllerIT extends ContainersEnvironment {

    @Autowired
    WebTestClient webClient;

    @Autowired
    JpaScheduleRepository jpaScheduleRepository;

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
        jpaScheduleRepository.deleteAll();
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
                    "major": "schedule-major",
                    "groupNumber": 1,
                    "wd": 0
                }
                """;

        webClient.post().uri("/api/admin/commands/schedules")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isForbidden();

        assertThat(scheduleRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotCreateScheduleWithUnavailableWithUnavailablePlanPolslId() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        scheduleRepository.save(Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major("major")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(1999)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
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

        webClient.post().uri("/api/admin/commands/schedules")
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
                    "major": "available-major",
                    "groupNumber": 1,
                    "wd": 0
                }
                """;

        webClient.post().uri("/api/admin/commands/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNoContent();

        List<Schedule> schedules = scheduleRepository.findAll();
        ConsumerRecord<String, ScheduleEvent> consumerRecord =
                KafkaTestUtils.getSingleRecord(scheduleEventConsumer, SCHEDULE_EVENT_TOPIC, Duration.ofSeconds(20));

        assertThat(schedules)
                .hasSize(1);

        Schedule schedule = schedules.get(0);
        PlanPolslData planPolslData = schedule.getPlanPolslData();

        assertThat(schedule.getSemester())
                .isEqualTo(2);
        assertThat(schedule.getMajor())
                .isEqualTo("available-major");
        assertThat(schedule.getGroupNumber())
                .isEqualTo(1);

        assertThat(planPolslData.getType())
                .isEqualTo(1);
        assertThat(planPolslData.getId())
                .isEqualTo(1999);
        assertThat(planPolslData.getWeekDays())
                .isZero();

        ScheduleEvent expectedEvent = ScheduleEvent.builder()
                .type(EventType.CREATE)
                .scheduleId(schedule.getId().toString())
                .entity(schedule)
                .build();

        assertThat(consumerRecord.key()).isEqualTo(schedule.getId().toString());
        assertThat(consumerRecord.value()).isEqualTo(expectedEvent);
    }

    @Test
    void shouldNotAllowStandardUserToDeleteSchedule() {
        String token = KeycloakUtils.getUserToken(keycloakContainer.getAuthServerUrl());

        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major("schedule-major")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(1999)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        scheduleRepository.save(schedule);

        webClient.delete().uri("/api/admin/commands/schedules/%s".formatted(schedule.getId()))
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();

        assertThat(scheduleRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldNotDeleteNonExistingSchedule() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major("schedule-major")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(1999)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        scheduleRepository.save(schedule);

        webClient.delete().uri("/api/admin/commands/schedules/%s".formatted(UUID.randomUUID()))
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();

        assertThat(scheduleRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldDeleteScheduleAndProduceEvent() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major("schedule-major")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(1999)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        Schedule otherSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major("schedule-major-2")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(2000)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        scheduleRepository.save(schedule);
        scheduleRepository.save(otherSchedule);

        webClient.delete().uri("/api/admin/commands/schedules/%s".formatted(schedule.getId()))
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        ConsumerRecord<String, ScheduleEvent> consumerRecord =
                KafkaTestUtils.getSingleRecord(scheduleEventConsumer, SCHEDULE_EVENT_TOPIC, Duration.ofSeconds(20));
        ScheduleEvent event = consumerRecord.value();

        assertThat(scheduleRepository.findAll()).hasSize(1);

        assertThat(consumerRecord.key()).isEqualTo(schedule.getId().toString());
        assertThat(event.scheduleId()).isEqualTo(schedule.getId().toString());
        assertThat(event.type()).isEqualTo(EventType.DELETE);
        assertThat(event.entity()).isEqualTo(schedule);
    }

    @Test
    void shouldNotAllowStandardUserToUpdateSchedule() {
        String token = KeycloakUtils.getUserToken(keycloakContainer.getAuthServerUrl());

        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major("schedule-major")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(1999)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        scheduleRepository.save(schedule);

        String payload = """
                {
                    "type": 1,
                    "planPolslId": 1999,
                    "semester": 2,
                    "major": "schedule-major",
                    "groupNumber": 1,
                    "wd": 0
                }
                """;

        webClient.put().uri("/api/admin/commands/schedules/%s".formatted(schedule.getId()))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldNotUpdateScheduleWithUnavailablePlanPolslId() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major("schedule-name")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(1999)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        Schedule otherSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major("other-schedule-name")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(23232)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        scheduleRepository.save(schedule);
        scheduleRepository.save(otherSchedule);

        String payload = """
                {
                    "type": 1,
                    "planPolslId": 23232,
                    "semester": 2,
                    "name": "other-schedule-name",
                    "groupNumber": 1,
                    "wd": 0
                }
                """;

        webClient.put().uri("/api/admin/commands/schedules/%s".formatted(schedule.getId()))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldNotNotExistingSchedule() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major("schedule-major")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(1999)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        scheduleRepository.save(schedule);

        String payload = """
                {
                    "type": 1,
                    "planPolslId": 1999,
                    "semester": 2,
                    "major": "schedule-major",
                    "groupNumber": 1,
                    "wd": 0
                }
                """;

        webClient.put().uri("/api/admin/commands/schedules/%s".formatted(UUID.randomUUID()))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldUpdateScheduleAndProduceEvent() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major("schedule-major")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(1999)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        Schedule otherSchedule = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major("other-schedule-major")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(31231)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        scheduleRepository.save(schedule);
        scheduleRepository.save(otherSchedule);

        String payload = """
                {
                    "type": 53,
                    "planPolslId": 1234,
                    "semester": 12,
                    "major": "new-major",
                    "groupNumber": 22,
                    "wd": 15
                }
                """;

        webClient.put().uri("/api/admin/commands/schedules/%s".formatted(schedule.getId()))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNoContent();

        Optional<Schedule> optionalUpdatedSchedule = scheduleRepository.findById(schedule.getId());
        Optional<Schedule> optionalOtherNotUpdatedSchedule = scheduleRepository.findById(otherSchedule.getId());

        ConsumerRecord<String, ScheduleEvent> consumerRecord =
                KafkaTestUtils.getSingleRecord(scheduleEventConsumer, SCHEDULE_EVENT_TOPIC, Duration.ofSeconds(20));
        ScheduleEvent event = consumerRecord.value();

        assertThat(optionalUpdatedSchedule).isPresent();
        assertThat(optionalOtherNotUpdatedSchedule).isPresent();

        Schedule updatedSchedule = optionalUpdatedSchedule.get();
        Schedule otherNotUpdatedSchedule = optionalOtherNotUpdatedSchedule.get();

        assertThat(otherNotUpdatedSchedule)
                .usingRecursiveComparison()
                .isEqualTo(otherSchedule);

        Schedule expectedUpdate = Schedule.builder()
                .id(schedule.getId())
                .semester(12)
                .major("new-major")
                .groupNumber(22)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(1234)
                                .type(53)
                                .weekDays(15)
                                .build()
                )
                .build();

        assertThat(updatedSchedule)
                .usingRecursiveComparison()
                .isEqualTo(expectedUpdate);

        assertThat(event.type()).isEqualTo(EventType.UPDATE);
        assertThat(event.scheduleId()).isEqualTo(schedule.getId().toString());
        assertThat(event.entity())
                .usingRecursiveComparison()
                .isEqualTo(updatedSchedule);
    }

}