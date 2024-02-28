package com.github.karixdev.scheduleservice.infrastructure.rest.controller;

import com.github.karixdev.scheduleservice.ContainersEnvironment;
import com.github.karixdev.scheduleservice.application.event.EventType;
import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import com.github.karixdev.scheduleservice.infrastructure.dal.JpaScheduleRepository;
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
                    "name": "schedule-name",
                    "groupNumber": 1,
                    "wd": 0
                }
                """;

        webClient.post().uri("/api/commands/schedules")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isForbidden();

        assertThat(scheduleRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotCreateScheduleWithUnavailableName() {
        String token = KeycloakUtils.getAdminToken(keycloakContainer.getAuthServerUrl());

        scheduleRepository.save(Schedule.builder()
                .id(UUID.randomUUID())
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

        webClient.post().uri("/api/commands/schedules")
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

        webClient.post().uri("/api/commands/schedules")
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
                .type(EventType.CREATE)
                .scheduleId(schedule.getId().toString())
                .entity(schedule)
                .build();

        assertThat(consumerRecord.key()).isEqualTo(schedule.getId().toString());
        assertThat(consumerRecord.value()).isEqualTo(expectedEvent);
    }

}