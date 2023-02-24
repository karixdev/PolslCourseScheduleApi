package com.github.karixdev.scheduleservice.schedule;

import com.github.karixdev.scheduleservice.ContainersEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ScheduleControllerIT extends ContainersEnvironment {
    @Autowired
    WebTestClient webClient;

    @Autowired
    ScheduleRepository scheduleRepository;

    @AfterEach
    void tearDown() {
        scheduleRepository.deleteAll();
    }

    @Test
    void shouldNotCreateSchedule() {
        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule-name")
                .groupNumber(1)
                .build());

        String payload = """
                {
                    "type": 1,
                    "plan_polsl_id": 1999,
                    "semester": 2,
                    "name": "schedule-name",
                    "group_number": 1
                }
                """;

        webClient.post().uri("/api/v2/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();

        assertThat(scheduleRepository.findAll())
                .hasSize(1);
    }

    @Test
    void shouldCreateSchedule() {
        String payload = """
                {
                    "type": 1,
                    "plan_polsl_id": 1999,
                    "semester": 2,
                    "name": "available-name",
                    "group_number": 1
                }
                """;

        webClient.post().uri("/api/v2/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.semester").isEqualTo(2)
                .jsonPath("$.name").isEqualTo("available-name")
                .jsonPath("$.group_number").isEqualTo(1);

        List<Schedule> schedules = scheduleRepository.findAll();

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
                        .build(),
                Schedule.builder()
                        .type(1)
                        .planPolslId(1999)
                        .semester(1)
                        .name("schedule2")
                        .groupNumber(1)
                        .build(),
                Schedule.builder()
                        .type(1)
                        .planPolslId(1000)
                        .semester(2)
                        .name("schedule3")
                        .groupNumber(1)
                        .build()
        ));

        webClient.get().uri("/api/v2/schedules")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.*").isArray()
                .jsonPath("$[0].semester").isEqualTo(1)
                .jsonPath("$[0].group_number").isEqualTo(1)
                .jsonPath("$[1].semester").isEqualTo(1)
                .jsonPath("$[1].group_number").isEqualTo(2)
                .jsonPath("$[2].semester").isEqualTo(2)
                .jsonPath("$[2].group_number").isEqualTo(1);
    }

    @Test
    void shouldNotFindScheduleById() {
        String id = UUID.randomUUID().toString();

        webClient.get().uri("/api/v2/schedules/" + id)
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
                .build());

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .build());

        webClient.get().uri("/api/v2/schedules/" + schedule.getId().toString())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(schedule.getId().toString())
                .jsonPath("$.semester").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("schedule1")
                .jsonPath("$.group_number").isEqualTo(2);
    }

    @Test
    void shouldNotUpdateNotExistingSchedule() {
        UUID id = UUID.randomUUID();

        String payload = """
                {
                    "type": 1,
                    "plan_polsl_id": 1999,
                    "semester": 2,
                    "name": "schedule-name",
                    "group_number": 1
                }
                """;

        webClient.put().uri("/api/v2/schedules/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldNotUpdateScheduleWhileTryingToAssignUnavailableName() {
        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(2000)
                .semester(1)
                .name("schedule1")
                .groupNumber(2)
                .build());

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .build());

        String payload = """
                {
                    "type": 1,
                    "plan_polsl_id": 1999,
                    "semester": 2,
                    "name": "schedule2",
                    "group_number": 1
                }
                """;

        webClient.put().uri("/api/v2/schedules/" + schedule.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldUpdateSchedule() {
        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(2000)
                .semester(1)
                .name("schedule1")
                .groupNumber(2)
                .build());

        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .build());

        String payload = """
                {
                    "type": 2,
                    "plan_polsl_id": 1999,
                    "semester": 5,
                    "name": "schedule3",
                    "group_number": 7
                }
                """;

        webClient.put().uri("/api/v2/schedules/" + schedule.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(schedule.getId().toString())
                .jsonPath("$.semester").isEqualTo(5)
                .jsonPath("$.name").isEqualTo("schedule3")
                .jsonPath("$.group_number").isEqualTo(7);
    }

    @Test
    void shouldNotDeleteNotExistingSchedule() {
        scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .build());

        webClient.delete().uri("/api/v2/schedules/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldDeleteSchedule() {
        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .build());

        webClient.delete().uri("/api/v2/schedules/" + schedule.getId())
                .exchange()
                .expectStatus().isNoContent();
    }
}
