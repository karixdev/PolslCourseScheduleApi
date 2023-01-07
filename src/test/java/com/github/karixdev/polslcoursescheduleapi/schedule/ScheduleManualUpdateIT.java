package com.github.karixdev.polslcoursescheduleapi.schedule;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
import com.github.karixdev.polslcoursescheduleapi.course.CourseRepository;
import com.github.karixdev.polslcoursescheduleapi.discord.DiscordWebhook;
import com.github.karixdev.polslcoursescheduleapi.discord.DiscordWebhookRepository;
import com.github.karixdev.polslcoursescheduleapi.jwt.JwtService;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.user.UserRepository;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.UserService;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

/*
This class is only responsible for testing POST /api/v1/schedule/{id}
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@WireMockTest(httpPort = 8888)
public class ScheduleManualUpdateIT extends ContainersEnvironment {
    @Autowired
    WebTestClient webClient;

    @Autowired
    UserService userService;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DiscordWebhookRepository discordWebhookRepository;

    @DynamicPropertySource
    static void overridePlanPolslBaseUrl(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("plan-polsl.base-url", () -> "http://localhost:8888/plan");
    }

    @AfterEach
    void tearDown() {
        discordWebhookRepository.deleteAll();
        courseRepository.deleteAll();
        scheduleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldNotManuallyUpdateScheduleForUnauthorizedUser() {
        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_USER,
                        true
                ));

        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .name("schedule")
                .addedBy(userPrincipal.getUser())
                .planPolslId(101)
                .type(0)
                .groupNumber(4)
                .semester(1)
                .build());

        String token = jwtService.createToken(userPrincipal);

        webClient.post().uri("/api/v1/schedule/" + schedule.getId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldManuallyUpdateNotExistingSchedule() {
        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_ADMIN,
                        true
                ));

        String token = jwtService.createToken(userPrincipal);

        webClient.post().uri("/api/v1/schedule/1337")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldManuallyUpdateSchedule() {
        UserPrincipal userPrincipal = new UserPrincipal(
                userService.createUser(
                        "email@email.com",
                        "password",
                        UserRole.ROLE_ADMIN,
                        true
                ));

        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .name("schedule")
                .addedBy(userPrincipal.getUser())
                .planPolslId(101)
                .type(0)
                .groupNumber(4)
                .semester(1)
                .build());

        discordWebhookRepository.save(DiscordWebhook.builder()
                .addedBy(userPrincipal.getUser())
                .url("http://localhost:8888/discord-api/123")
                .schedules(Set.of(schedule))
                .build());

        stubFor(get(urlPathEqualTo("/plan"))
                .withQueryParam("id", equalTo("101"))
                .withQueryParam("type", equalTo("0"))
                .withQueryParam("winH", equalTo("1000"))
                .withQueryParam("winW", equalTo("1000"))
                .willReturn(ok().withBody("""
                        <div class="cd">07:00-08:00</div>
                        <div id="course_1" class="coursediv" mtp="1" resizable="0" zold="2" cwb="154" chb="56" cw="154" ch="56" style="width: 154px; height: 56px; top: 552px; left: 420px; border: 1px solid rgb(102, 102, 102); background-color: rgb(123, 247, 141); display: block; z-index: 2;">course_1</div>
                        <div id="course_2" class="coursediv" mtp="1" resizable="0" zold="3" cwb="154" chb="56" cw="154" ch="56" style="width: 154px; height: 56px; top: 372px; left: 88px; border: 1px solid rgb(102, 102, 102); background-color: rgb(123, 247, 141); display: block; z-index: 3;">course_2</div>
                        <div id="course_3" class="coursediv" mtp="1" resizable="0" zold="4" cwb="154" chb="56" cw="154" ch="56" style="width: 154px; height: 56px; top: 462px; left: 586px; border: 1px solid rgb(102, 102, 102); background-color: rgb(124, 176, 246); display: block; z-index: 4;">course_3</div>
                        <div id="course_4" class="coursediv" mtp="1" resizable="0" zold="5" cwb="154" chb="56" cw="154" ch="56" style="width: 154px; height: 56px; top: 473px; left: 420px; border: 1px solid rgb(102, 102, 102); background-color: rgb(123, 247, 141); display: block; z-index: 5;">course_4</div>
                        """)));

        stubFor(post(urlPathEqualTo("/discord-api/123"))
                .willReturn(noContent()));

        String token = jwtService.createToken(userPrincipal);

        webClient.post().uri("/api/v1/schedule/" + schedule.getId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(courseRepository.findAll())
                                .hasSize(4));
    }
}
