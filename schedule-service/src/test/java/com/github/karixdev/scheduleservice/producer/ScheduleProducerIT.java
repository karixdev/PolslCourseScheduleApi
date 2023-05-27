package com.github.karixdev.scheduleservice.producer;

import com.github.karixdev.scheduleservice.ContainersEnvironment;
import com.github.karixdev.scheduleservice.entity.Schedule;
import com.github.karixdev.scheduleservice.message.ScheduleUpdateRequestMessage;
import com.github.karixdev.scheduleservice.producer.ScheduleProducer;
import com.github.karixdev.scheduleservice.repository.ScheduleRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import static com.github.karixdev.scheduleservice.props.ScheduleMQProperties.SCHEDULE_UPDATE_REQUEST_QUEUE;
import static com.github.karixdev.scheduleservice.props.ScheduleMQProperties.SCHEDULE_UPDATE_RESPONSE_QUEUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ScheduleProducerIT extends ContainersEnvironment {
    @Autowired
    ScheduleProducer producer;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @AfterEach
    void tearDown() {
        scheduleRepository.deleteAll();

        rabbitAdmin.purgeQueue(SCHEDULE_UPDATE_RESPONSE_QUEUE, true);
        rabbitAdmin.purgeQueue(SCHEDULE_UPDATE_REQUEST_QUEUE, true);
    }

    @Test
    void shouldProduceMessageToQueue() {
        Schedule schedule = scheduleRepository.save(Schedule.builder()
                .type(1)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(2)
                .build());

        producer.sendScheduleUpdateRequest(schedule);

        var expectedMsg = new ScheduleUpdateRequestMessage(
                schedule.getId(),
                schedule.getType(),
                schedule.getPlanPolslId(),
                schedule.getWd()
        );

        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(getScheduleUpdateRequestMessage())
                        .isEqualTo(expectedMsg)
                );
    }

    private ScheduleUpdateRequestMessage getScheduleUpdateRequestMessage() {
        var typeReference = new ParameterizedTypeReference<ScheduleUpdateRequestMessage>() {
            @Override
            public @NotNull Type getType() {
                return super.getType();
            }
        };

        return rabbitTemplate.receiveAndConvert(SCHEDULE_UPDATE_REQUEST_QUEUE, typeReference);
    }
}
