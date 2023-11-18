package com.github.karixdev.webscraperservice.consumer;

import com.github.karixdev.webscraperservice.ContainersEnvironment;
import com.github.karixdev.webscraperservice.message.RawCoursesMessage;
import com.github.karixdev.webscraperservice.message.ScheduleEventMessage;
import com.github.karixdev.webscraperservice.model.CourseCell;
import com.github.karixdev.webscraperservice.model.Link;
import com.github.karixdev.webscraperservice.model.TimeCell;
import com.github.karixdev.webscraperservice.props.CoursesMQProperties;
import com.github.karixdev.webscraperservice.props.ScheduleEventMQProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.karixdev.webscraperservice.props.CoursesMQProperties.RAW_COURSES_QUEUE;
import static com.github.karixdev.webscraperservice.props.PlanPolslClientProperties.WIN_H;
import static com.github.karixdev.webscraperservice.props.PlanPolslClientProperties.WIN_W;
import static com.github.karixdev.webscraperservice.props.ScheduleEventMQProperties.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
class ScheduleEventConsumerIT extends ContainersEnvironment {
    @Autowired
    RabbitTemplate template;

    @Autowired
    RabbitAdmin admin;

    WireMockServer wm = new WireMockServer(9999);

    UUID exampleScheduleId;
    ScheduleEventMessage exampleMessage;

    RawCoursesMessage exampleExpectedMessage;

    @DynamicPropertySource
    static void overridePlanPolslUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "plan-polsl-url",
                () -> "http://localhost:9999"
        );
    }

    @BeforeEach
    void setUp() {
        wm.start();

        admin.purgeQueue(SCHEDULE_UPDATE_QUEUE, true);
        admin.purgeQueue(SCHEDULE_CREATE_QUEUE, true);
        admin.purgeQueue(RAW_COURSES_QUEUE, true);

        exampleScheduleId = UUID.randomUUID();
        exampleMessage = new ScheduleEventMessage(
                exampleScheduleId,
                0,
                18843,
                4
        );

        exampleExpectedMessage = new RawCoursesMessage(
                exampleScheduleId,
                Set.of(
                        new TimeCell("08:00-09:00"),
                        new TimeCell("07:00-08:00"),
                        new TimeCell("10:00-11:00")
                ),
                Set.of(
                        new CourseCell(
                                259,
                                254,
                                135,
                                154,
                                "This is course div, lab",
                                Set.of(
                                        new Link(
                                                "dr. Adam",
                                                "plan.php?type=10&id=10"
                                        ),
                                        new Link(
                                                "314MS",
                                                "plan.php?type=20&id=10"
                                        )

                                )
                        )
                )
        );
    }

    @AfterEach
    void tearDown() {
        wm.stop();
    }

    @Test
    void shouldListenForScheduleCreateEventAndThenProduceRawCoursesMessage() {
        // Given
        ScheduleEventMessage message = exampleMessage;

        stubPlanPolslResponse(message);

        // When
        template.convertAndSend(
                SCHEDULE_EXCHANGE,
                SCHEDULE_CREATE_ROUTING_KEY,
                message
        );

        // Then
        RawCoursesMessage expectedMessage = exampleExpectedMessage;

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(getScheduleUpdateResponseMessage())
                    .isEqualTo(expectedMessage);
        });
    }

    @Test
    void shouldListenForScheduleUpdateEventAndThenProduceRawCoursesMessage() {
        // Given
        ScheduleEventMessage message = exampleMessage;

        stubPlanPolslResponse(message);

        // When
        template.convertAndSend(
                SCHEDULE_EXCHANGE,
                SCHEDULE_UPDATE_ROUTING_KEY,
                message
        );

        // Then
        RawCoursesMessage expectedMessage = exampleExpectedMessage;

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(getScheduleUpdateResponseMessage())
                    .isEqualTo(expectedMessage);
        });
    }

    private void stubPlanPolslResponse(ScheduleEventMessage message) {
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

    }

    private RawCoursesMessage getScheduleUpdateResponseMessage() {
        var typeReference = new ParameterizedTypeReference<RawCoursesMessage>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        };

        return template.receiveAndConvert(RAW_COURSES_QUEUE, typeReference);
    }
}