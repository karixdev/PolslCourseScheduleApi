package com.github.karixdev.scheduleservice.schedule;

import com.github.karixdev.scheduleservice.ContainersEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.karixdev.scheduleservice.schedule.props.ScheduleMQProperties.SCHEDULE_UPDATE_REQUEST_QUEUE;
import static com.github.karixdev.scheduleservice.schedule.props.ScheduleMQProperties.SCHEDULE_UPDATE_RESPONSE_QUEUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ScheduleJobIT extends ContainersEnvironment {
    @Autowired
    ScheduleService scheduleService;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @DynamicPropertySource
    static void overrideScheduleJobCron(DynamicPropertyRegistry registry) {
        registry.add(
                "schedule.job.cron",
                () -> "*/10 * * * * *");
    }

    @BeforeEach
    void setUp() {
        rabbitAdmin.purgeQueue(SCHEDULE_UPDATE_RESPONSE_QUEUE, true);
        rabbitAdmin.purgeQueue(SCHEDULE_UPDATE_REQUEST_QUEUE, true);
    }

    @AfterEach
    void tearDown() {
        scheduleRepository.deleteAll();
    }

    @Test
    void shouldPush() {
        scheduleRepository.saveAll(List.of(
                Schedule.builder()
                        .type(1)
                        .planPolslId(1)
                        .semester(1)
                        .name("schedule-name-1")
                        .groupNumber(1)
                        .wd(0)
                        .build(),
                Schedule.builder()
                        .type(1)
                        .planPolslId(2)
                        .semester(1)
                        .name("schedule-name-2")
                        .groupNumber(1)
                        .wd(0)
                        .build(),
                Schedule.builder()
                        .type(1)
                        .planPolslId(3)
                        .semester(1)
                        .name("schedule-name-3")
                        .groupNumber(1)
                        .wd(0)
                        .build()
        ));

        await().atMost(40, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    int count = (int) rabbitAdmin
                            .getQueueProperties(SCHEDULE_UPDATE_REQUEST_QUEUE)
                            .get("QUEUE_MESSAGE_COUNT");

                    assertThat(count).isEqualTo(3);
                });
    }
}
