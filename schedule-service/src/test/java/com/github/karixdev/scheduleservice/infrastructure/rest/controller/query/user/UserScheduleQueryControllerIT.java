package com.github.karixdev.scheduleservice.infrastructure.rest.controller.query.user;

import com.github.karixdev.scheduleservice.ContainersEnvironment;
import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import com.github.karixdev.scheduleservice.infrastructure.dal.entity.ScheduleEntity;
import com.github.karixdev.scheduleservice.infrastructure.dal.repository.JpaScheduleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserScheduleQueryControllerIT extends ContainersEnvironment {

    @Autowired
    WebTestClient webClient;

    @Autowired
    JpaScheduleRepository jpaScheduleRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Test
    void shouldReturnMajorsInCorrectOrder() {
        Schedule schedule1 = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major("a-major")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(1231)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        Schedule schedule2 = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major("c-major")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(4324)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        Schedule schedule3 = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major("a-major")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(64564)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        Schedule schedule4 = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major("b-major")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(7657)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        scheduleRepository.save(schedule1);
        scheduleRepository.save(schedule2);
        scheduleRepository.save(schedule3);
        scheduleRepository.save(schedule4);

        webClient.get().uri("/api/queries/schedules/majors")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.[0]").isEqualTo("a-major")
                .jsonPath("$.[1]").isEqualTo("b-major")
                .jsonPath("$.[2]").isEqualTo("c-major");
    }

    @Test
    void shouldReturnSemestersOfMajorInCorrectOrder() {
        String major = "major-a";

        Schedule schedule1 = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(3)
                .major(major)
                .groupNumber(3)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(1231)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        Schedule schedule2 = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(1)
                .major(major)
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(4324)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        Schedule schedule3 = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(2)
                .major(major)
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(64564)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        Schedule schedule4 = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(0)
                .major("major-b")
                .groupNumber(1)
                .planPolslData(
                        PlanPolslData.builder()
                                .id(7657)
                                .type(1)
                                .weekDays(0)
                                .build()
                )
                .build();

        scheduleRepository.save(schedule1);
        scheduleRepository.save(schedule2);
        scheduleRepository.save(schedule3);
        scheduleRepository.save(schedule4);

        webClient.get().uri("/api/queries/schedules/majors/%s/semesters".formatted(major))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.[0]").isEqualTo(1)
                .jsonPath("$.[1]").isEqualTo(2)
                .jsonPath("$.[2]").isEqualTo(3);
    }

}
