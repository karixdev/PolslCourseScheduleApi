package com.github.karixdev.polslcoursescheduleapi.schedule;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
import com.github.karixdev.polslcoursescheduleapi.course.CourseRepository;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.request.ScheduleRequest;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.UserService;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@WireMockTest(httpPort = 8888)
public class ScheduleJobIT extends ContainersEnvironment {
    @Autowired
    ScheduleService scheduleService;

    @Autowired
    UserService userService;

    @Autowired
    CourseRepository courseRepository;

    @DynamicPropertySource
    static void overrideScheduleJobCron(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("schedule-job.cron", () -> "*/3 * * * * *");
    }

    @DynamicPropertySource
    static void overridePlanPolslBaseUrl(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("plan-polsl.base-url", () -> "http://localhost:8888/plan");
    }

    @Test
    void shouldUpdateSchedulesCourses() {
        User user = userService.createUser(
                "email@email.com",
                "password",
                UserRole.ROLE_ADMIN,
                true
        );

        scheduleService.add(
                new ScheduleRequest(0, 101, 1, "schedule", 1),
                new UserPrincipal(user)
        );

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

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(courseRepository.findAll()).hasSize(4);
        });
    }
}
