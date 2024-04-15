package com.github.karixdev.scheduleservice.infrastructure.rest.controller.admin;

import com.github.karixdev.scheduleservice.ContainersEnvironment;
import com.github.karixdev.scheduleservice.application.event.EventType;
import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import com.github.karixdev.scheduleservice.infrastructure.dal.entity.ScheduleEntity;
import com.github.karixdev.scheduleservice.infrastructure.dal.repository.JpaScheduleRepository;
import com.github.karixdev.scheduleservice.utils.KeycloakUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
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
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ScheduleAdminControllerIT extends ContainersEnvironment {

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

        ConsumerFactory<String, ScheduleEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new JsonDeserializer<>(ScheduleEvent.class, false)
        );
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

        webClient.post().uri("/api/admin/schedules")
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

        webClient.post().uri("/api/admin/schedules")
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

        webClient.post().uri("/api/admin/schedules")
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

        webClient.delete().uri("/api/admin/schedules/%s".formatted(schedule.getId()))
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

        webClient.delete().uri("/api/admin/schedules/%s".formatted(UUID.randomUUID()))
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

        webClient.delete().uri("/api/admin/schedules/%s".formatted(schedule.getId()))
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

        webClient.put().uri("/api/admin/schedules/%s".formatted(schedule.getId()))
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

        webClient.put().uri("/api/admin/schedules/%s".formatted(schedule.getId()))
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

        webClient.put().uri("/api/admin/schedules/%s".formatted(UUID.randomUUID()))
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

        webClient.put().uri("/api/admin/schedules/%s".formatted(schedule.getId()))
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

    @Test
    void shouldBlankUpdateSchedules() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        List<ScheduleEntity> schedules = IntStream.range(0, 15).mapToObj(i ->
                ScheduleEntity.builder()
                        .id(UUID.randomUUID())
                        .type(i + 1)
                        .planPolslId(i + 1)
                        .semester(i + 1)
                        .major("major-" + i)
                        .groupNumber(i + 1)
                        .wd(i + 1)
                        .build()
        ).toList();

        jpaScheduleRepository.saveAll(schedules);

        List<ScheduleEntity> selectedSchedules = schedules.subList(0, 10);

        List<String> ids = selectedSchedules.stream()
                .map(entity -> entity.getId().toString())
                .toList();

        String queryString = String.join("&id=", ids);

        webClient.put().uri("/api/admin/schedules/blank-update?id=" + queryString)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();

        ConsumerRecords<String, ScheduleEvent> events = KafkaTestUtils.getRecords(scheduleEventConsumer, Duration.ofSeconds(20));
        Iterator<ConsumerRecord<String, ScheduleEvent>> iterator = events.iterator();

        List<String> eventsSchedulesIds = new ArrayList<>();
        while (iterator.hasNext()) {
            eventsSchedulesIds.add(iterator.next().value().scheduleId());
        }

        assertThat(eventsSchedulesIds).isEqualTo(ids);
    }

    @Test
    void shouldNotAllowStandardUserToFilterSchedules() {
        String token = KeycloakUtils.getUserToken(keycloakContainer.getAuthServerUrl());

        webClient.get().uri("/api/admin/schedules")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldFilterSchedules() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        String major1 = "major-1";
        String major2 = "major-3";

        int semester1 = 1;
        int semester2 = 2;

        int group1 = 3;
        int group2 = 4;

        int planPolslId1 = 5;
        int planPolslId2 = 6;

        int planPolslType1 = 7;
        int planPolslType2 = 8;

        int planPolslWeekDays1 = 9;
        int planPolslWeekDays2 = 10;

        ScheduleEntity schedule1 = ScheduleEntity.builder()
                .id(id1)
                .type(planPolslType1)
                .planPolslId(planPolslId1)
                .semester(semester1)
                .major(major1)
                .groupNumber(group1)
                .wd(planPolslWeekDays1)
                .build();

        ScheduleEntity schedule2 = ScheduleEntity.builder()
                .id(id2)
                .type(planPolslType2)
                .planPolslId(planPolslId2)
                .semester(semester2)
                .major(major2)
                .groupNumber(group2)
                .wd(planPolslWeekDays2)
                .build();

        ScheduleEntity schedule3 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(432523)
                .planPolslId(3423)
                .semester(semester2)
                .major(major2)
                .groupNumber(45)
                .wd(planPolslWeekDays2)
                .build();

        ScheduleEntity schedule4 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(432423)
                .planPolslId(32552)
                .semester(semester1)
                .major(major2)
                .groupNumber(group1)
                .wd(planPolslWeekDays2)
                .build();

        ScheduleEntity schedule5 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(432423)
                .planPolslId(756765)
                .semester(432345)
                .major("major-5")
                .groupNumber(54353)
                .wd(53467)
                .build();

        jpaScheduleRepository.saveAll(List.of(schedule1, schedule2, schedule3, schedule4, schedule5));

        String idQuery = "id=%s&id=%s".formatted(id1, id2);
        String majorQuery = "major=%s&major=%s".formatted(major1, major2);
        String semesterQuery = "semester=%d&semester=%d".formatted(semester1, semester2);
        String groupQuery = "group=%d&group=%d".formatted(group1, group2);

        String planPolslIdQuery = "plan-polsl-id=%d&plan-polsl-id=%d".formatted(planPolslId1, planPolslId2);
        String planPolslTypeQuery = "plan-polsl-type=%d&plan-polsl-type=%d".formatted(planPolslType1, planPolslType2);
        String planPolslWeedDaysQuery = "plan-polsl-week-days=%d&plan-polsl-week-days=%d".formatted(planPolslWeekDays1, planPolslWeekDays2);

        List<String> queries = List.of(
                idQuery,
                majorQuery,
                semesterQuery,
                groupQuery,
                planPolslIdQuery,
                planPolslTypeQuery,
                planPolslWeedDaysQuery
        );

        String completeQuery = String.join("&", queries);

        webClient.get().uri("/api/admin/schedules?" + completeQuery)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()

                .jsonPath("$.content.[0].id").isEqualTo(schedule1.getId().toString())
                .jsonPath("$.content.[0].major").isEqualTo(schedule1.getMajor())
                .jsonPath("$.content.[0].semester").isEqualTo(schedule1.getSemester())
                .jsonPath("$.content.[0].group").isEqualTo(schedule1.getGroupNumber())
                .jsonPath("$.content.[0].planPolslData.id").isEqualTo(schedule1.getPlanPolslId())
                .jsonPath("$.content.[0].planPolslData.type").isEqualTo(schedule1.getType())
                .jsonPath("$.content.[0].planPolslData.weekDays").isEqualTo(schedule1.getWd())

                .jsonPath("$.content.[1].id").isEqualTo(schedule2.getId().toString())
                .jsonPath("$.content.[1].major").isEqualTo(schedule2.getMajor())
                .jsonPath("$.content.[1].semester").isEqualTo(schedule2.getSemester())
                .jsonPath("$.content.[1].group").isEqualTo(schedule2.getGroupNumber())
                .jsonPath("$.content.[1].planPolslData.id").isEqualTo(schedule2.getPlanPolslId())
                .jsonPath("$.content.[1].planPolslData.type").isEqualTo(schedule2.getType())
                .jsonPath("$.content.[1].planPolslData.weekDays").isEqualTo(schedule2.getWd())

                .jsonPath("$.pageInfo.totalElements").isEqualTo(2);
    }

    @Test
    void shouldPaginateSchedules() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());


        List<ScheduleEntity> schedules = IntStream.range(0, 7).mapToObj(i ->
                ScheduleEntity.builder()
                        .id(UUID.randomUUID())
                        .type(i + 1)
                        .planPolslId(i + 1)
                        .semester(i + 1)
                        .major("major-" + i)
                        .groupNumber(i + 1)
                        .wd(i + 1)
                        .build()
        ).toList();

        jpaScheduleRepository.saveAll(schedules);

        webClient.get().uri("/api/admin/schedules?page=0&size=5")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.pageInfo.page").isEqualTo(0)
                .jsonPath("$.pageInfo.size").isEqualTo(5)
                .jsonPath("$.pageInfo.numberOfElements").isEqualTo(5)
                .jsonPath("$.pageInfo.totalElements").isEqualTo(7)
                .jsonPath("$.pageInfo.totalPages").isEqualTo(2)
                .jsonPath("$.pageInfo.isLast").isEqualTo(false);

        webClient.get().uri("/api/admin/schedules?page=1&size=5")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.pageInfo.page").isEqualTo(1)
                .jsonPath("$.pageInfo.size").isEqualTo(5)
                .jsonPath("$.pageInfo.numberOfElements").isEqualTo(2)
                .jsonPath("$.pageInfo.totalElements").isEqualTo(7)
                .jsonPath("$.pageInfo.totalPages").isEqualTo(2)
                .jsonPath("$.pageInfo.isLast").isEqualTo(true);
    }

}