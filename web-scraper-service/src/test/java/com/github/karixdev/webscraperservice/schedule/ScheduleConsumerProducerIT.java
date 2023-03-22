package com.github.karixdev.webscraperservice.schedule;


import com.github.karixdev.webscraperservice.ContainersEnvironment;
import com.github.karixdev.webscraperservice.course.domain.Course;
import com.github.karixdev.webscraperservice.course.domain.CourseType;
import com.github.karixdev.webscraperservice.course.domain.WeekType;
import com.github.karixdev.webscraperservice.schedule.message.ScheduleUpdateRequestMessage;
import com.github.karixdev.webscraperservice.schedule.message.ScheduleUpdateResponseMessage;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.lang.reflect.Type;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.karixdev.webscraperservice.planpolsl.properties.PlanPolslClientProperties.WIN_H;
import static com.github.karixdev.webscraperservice.planpolsl.properties.PlanPolslClientProperties.WIN_W;
import static com.github.karixdev.webscraperservice.schedule.props.ScheduleMQProperties.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
public class ScheduleConsumerProducerIT extends ContainersEnvironment {
    @Autowired
    RabbitTemplate template;

    @Autowired
    RabbitAdmin admin;

    @Autowired
    ScheduleService service;

    WireMockServer wm;

    @DynamicPropertySource
    static void overridePlanPolslUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "plan-polsl-url",
                () -> "http://localhost:9090"
        );
    }

    @BeforeEach
    void setUp() {
        wm = new WireMockServer(9090);
        wm.start();

        admin.purgeQueue(SCHEDULE_UPDATE_REQUEST_QUEUE, true);
        admin.purgeQueue(SCHEDULE_UPDATE_RESPONSE_QUEUE, true);
    }
    @Test
    void WhenReceivedScheduleUpdateRequestFromMQ_ThenWebScrapesAndSendsResultToMQ() {
        // Given
        var id = UUID.randomUUID();

        var message = new ScheduleUpdateRequestMessage(
                id,
                0,
                18843,
                4
        );

        wm.stubFor(get(urlPathEqualTo("/plan.php"))
                .withQueryParam("id", equalTo(String.valueOf(message.planPolslId())))
                .withQueryParam("type", equalTo(String.valueOf(message.type())))
                .withQueryParam("wd", equalTo(String.valueOf(message.wd())))
                .withQueryParam("winH", equalTo(String.valueOf(WIN_H)))
                .withQueryParam("winW", equalTo(String.valueOf(WIN_W)))

                .willReturn(ok().withBody("""
                        <div class="cd">08:00-09:00</div>
                        <div class="cd">07:00-08:00</div>
                        <div class="cd">10:00-11:00</div>
                        <div class="coursediv" style="left: 254px; top: 259px;" cw="154" ch="135">
                            This is course div, lab
                            <a href="plan.php?type=10&id=10">dr. Adam</a>
                            <a href="plan.php?type=20&id=10">314MS</a>
                        </div>
                        """))
        );

        // When
        template.convertAndSend(
                SCHEDULE_TOPIC,
                SCHEDULE_UPDATE_REQUEST_ROUTING_KEY,
                message
        );

        // Then
        var expected = new ScheduleUpdateResponseMessage(
                id,
                Set.of(
                        new Course(
                                LocalTime.of(7, 30),
                                LocalTime.of(10, 45),
                                "This is course div",
                                CourseType.LAB,
                                "dr. Adam",
                                DayOfWeek.TUESDAY,
                                WeekType.EVERY,
                                "314MS",
                                null
                        )
                )
        );

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(getScheduleUpdateResponseMessage())
                    .isEqualTo(expected);
        });
    }

    private ScheduleUpdateResponseMessage getScheduleUpdateResponseMessage() {
        var typeReference = new ParameterizedTypeReference<ScheduleUpdateResponseMessage>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        };

        return template.receiveAndConvert(SCHEDULE_UPDATE_RESPONSE_QUEUE, typeReference);
    }
}
