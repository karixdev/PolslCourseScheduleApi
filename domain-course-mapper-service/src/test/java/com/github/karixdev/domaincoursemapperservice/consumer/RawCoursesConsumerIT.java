package com.github.karixdev.domaincoursemapperservice.consumer;

import com.github.karixdev.domaincoursemapperservice.ContainersEnvironment;
import com.github.karixdev.domaincoursemapperservice.message.DomainCoursesMessage;
import com.github.karixdev.domaincoursemapperservice.message.RawCoursesMessage;
import com.github.karixdev.domaincoursemapperservice.model.domain.Course;
import com.github.karixdev.domaincoursemapperservice.model.domain.CourseType;
import com.github.karixdev.domaincoursemapperservice.model.domain.WeekType;
import com.github.karixdev.domaincoursemapperservice.model.raw.CourseCell;
import com.github.karixdev.domaincoursemapperservice.model.raw.TimeCell;
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

import static com.github.karixdev.domaincoursemapperservice.props.CoursesMQProperties.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
class RawCoursesConsumerIT extends ContainersEnvironment {
    @Autowired
    RabbitTemplate template;

    @Autowired
    RabbitAdmin admin;

    @DynamicPropertySource
    static void overridePlanPolslUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "plan-polsl-url",
                () -> "http://localhost:9999"
        );
    }

    @BeforeEach
    void setUp() {
        admin.purgeQueue(DOMAIN_COURSES_QUEUE, true);
        admin.purgeQueue(RAW_COURSES_QUEUE, true);
    }

    @Test
    void WhenReceivedScheduleUpdateRequestFromMQ_ThenWebScrapesAndSendsResultToMQ() {
        // Given
        UUID scheduleId = UUID.randomUUID();

        CourseCell courseCell = new CourseCell(
                259,
                254,
                135,
                154,
                "course 1"
        );
        Set<CourseCell> courseCells = Set.of(courseCell);

        Set<TimeCell> timeCells = Set.of(new TimeCell("08:30-10:00"));

        RawCoursesMessage message = new RawCoursesMessage(
                scheduleId,
                timeCells,
                courseCells
        );

        // When
        template.convertAndSend(
                COURSES_EXCHANGE,
                RAW_COURSES_ROUTING_KEY,
                message
        );

        // Then
        Course course = new Course(
                LocalTime.of(8, 30),
                LocalTime.of(11, 45),
                "course 1",
                CourseType.INFO,
                "",
                DayOfWeek.TUESDAY,
                WeekType.EVERY,
                "",
                null
        );
        DomainCoursesMessage expected = new DomainCoursesMessage(
                scheduleId,
                Set.of(course)
        );

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(getScheduleUpdateResponseMessage())
                    .isEqualTo(expected);
        });
    }

    private DomainCoursesMessage getScheduleUpdateResponseMessage() {
        var typeReference = new ParameterizedTypeReference<DomainCoursesMessage>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        };

        return template.receiveAndConvert(DOMAIN_COURSES_QUEUE, typeReference);
    }
}