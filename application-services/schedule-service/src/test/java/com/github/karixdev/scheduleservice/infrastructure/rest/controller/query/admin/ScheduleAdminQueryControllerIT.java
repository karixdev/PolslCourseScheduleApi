package com.github.karixdev.scheduleservice.infrastructure.rest.controller.query.admin;

import com.github.karixdev.scheduleservice.ContainersEnvironment;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import com.github.karixdev.scheduleservice.infrastructure.dal.entity.ScheduleEntity;
import com.github.karixdev.scheduleservice.infrastructure.dal.repository.JpaScheduleRepository;
import com.github.karixdev.scheduleservice.utils.KeycloakUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ScheduleAdminQueryControllerIT extends ContainersEnvironment {

    @Autowired
    WebTestClient webClient;

    @Autowired
    JpaScheduleRepository jpaScheduleRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @AfterEach
    void tearDown() {
        jpaScheduleRepository.deleteAll();
    }

    @Test
    void shouldNotAllowStandardUserToFilterSchedules() {
        String token = KeycloakUtils.getUserToken(keycloakContainer.getAuthServerUrl());

        webClient.get().uri("/api/admin/queries/schedules")
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

        webClient.get().uri("/api/admin/queries/schedules?" + completeQuery)
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

        webClient.get().uri("/api/admin/queries/schedules?page=0&size=5")
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

        webClient.get().uri("/api/admin/queries/schedules?page=1&size=5")
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