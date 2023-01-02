package com.github.karixdev.polslcoursescheduleapi.schedule;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
import com.github.karixdev.polslcoursescheduleapi.jwt.JwtService;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleRepository;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.user.UserRepository;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ScheduleControllerIT extends ContainersEnvironment {
    @Autowired
    WebTestClient webClient;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    JwtService jwtService;

    @AfterEach
    void tearDown() {
        scheduleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldNotAddScheduleForStanderUser() {
        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_USER,
                        true
                ));

        String payload = """
                {
                    "type": 0,
                    "plan_polsl_id": 1,
                    "semester": 2,
                    "name": "schedule-name",
                    "group_number": 3
                }
                """;

        String token = jwtService.createToken(userPrincipal);

        webClient.post().uri("/api/v1/schedule")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldNotAddScheduleWithAlreadyExistingName() {
        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_ADMIN,
                        true
                ));

        scheduleRepository.save(Schedule.builder()
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule-name")
                .addedBy(userPrincipal.getUser())
                .build());

        String payload = """
                {
                    "type": 0,
                    "plan_polsl_id": 1,
                    "semester": 2,
                    "name": "schedule-name",
                    "group_number": 3
                }
                """;

        String token = jwtService.createToken(userPrincipal);

        webClient.post().uri("/api/v1/schedule")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldAddSchedule() {
        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_ADMIN,
                        true
                ));

        String payload = """
                {
                    "type": 0,
                    "plan_polsl_id": 1,
                    "semester": 2,
                    "name": "schedule-name",
                    "group_number": 3
                }
                """;

        String token = jwtService.createToken(userPrincipal);

        webClient.post().uri("/api/v1/schedule")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.semester").isEqualTo(2)
                .jsonPath("$.name").isEqualTo("schedule-name")
                .jsonPath("$.group_number").isEqualTo(3);
    }

    @Test
    void shouldNotDeleteForStandardUser() {
        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_USER,
                        true
                ));

        String token = jwtService.createToken(userPrincipal);

        webClient.delete().uri("/api/v1/schedule/1")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }


    @Test
    void shouldNotDeleteNotExistingSchedule() {
        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_ADMIN,
                        true
                ));

        String token = jwtService.createToken(userPrincipal);

        webClient.delete().uri("/api/v1/schedule/1")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldDeleteSchedule() {
        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_ADMIN,
                        true
                ));

        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule-name")
                .addedBy(userPrincipal.getUser())
                .build());

        String token = jwtService.createToken(userPrincipal);

        webClient.delete().uri("/api/v1/schedule/" + schedule.getId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldGetAllSchedules() {
        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_ADMIN,
                        true
                ));

        scheduleRepository.save(Schedule.builder()
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule-name")
                .addedBy(userPrincipal.getUser())
                .build());

        scheduleRepository.save(Schedule.builder()
                .type(0)
                .planPolslId(101)
                .semester(1)
                .groupNumber(2)
                .name("schedule-name-2")
                .addedBy(userPrincipal.getUser())
                .build());

        webClient.get().uri("/api/v1/schedule")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.semesters.1[0].id").isNotEmpty()
                .jsonPath("$.semesters.1[0].group_number").isEqualTo(2)
                .jsonPath("$.semesters.1[0].name").isEqualTo("schedule-name-2")
                .jsonPath("$.semesters.2[0].id").isNotEmpty()
                .jsonPath("$.semesters.2[0].group_number").isEqualTo(3)
                .jsonPath("$.semesters.2[0].name").isEqualTo("schedule-name");
    }
}
