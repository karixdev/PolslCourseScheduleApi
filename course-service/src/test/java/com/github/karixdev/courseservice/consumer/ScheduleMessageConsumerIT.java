package com.github.karixdev.courseservice.consumer;

import com.github.karixdev.courseservice.ContainersEnvironment;
import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.entity.CourseType;
import com.github.karixdev.courseservice.entity.WeekType;
import com.github.karixdev.courseservice.message.ScheduleEventMessage;
import com.github.karixdev.courseservice.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.karixdev.courseservice.props.ScheduleMQProperties.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ScheduleMessageConsumerIT extends ContainersEnvironment {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        rabbitAdmin.purgeQueue(SCHEDULE_UPDATE_QUEUE, true);

        courseRepository.deleteAll();
    }

    @Test
    void shouldDeleteCoursesWithScheduleIdFromMessage() {
        UUID scheduleId = UUID.randomUUID();
        ScheduleEventMessage message = new ScheduleEventMessage(scheduleId);

        courseRepository.saveAll(List.of(
                Course.builder()
                        .scheduleId(scheduleId)
                        .name("course-name-1")
                        .courseType(CourseType.INFO)
                        .dayOfWeek(DayOfWeek.FRIDAY)
                        .weekType(WeekType.EVERY)
                        .startsAt(LocalTime.of(8, 30))
                        .endsAt(LocalTime.of(10, 15))
                        .build(),
                Course.builder()
                        .scheduleId(scheduleId)
                        .name("course-name-2")
                        .courseType(CourseType.LAB)
                        .dayOfWeek(DayOfWeek.TUESDAY)
                        .weekType(WeekType.EVERY)
                        .startsAt(LocalTime.of(10, 30))
                        .endsAt(LocalTime.of(12, 15))
                        .build(),
                Course.builder()
                        .scheduleId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                        .name("course-name-3")
                        .courseType(CourseType.LECTURE)
                        .dayOfWeek(DayOfWeek.TUESDAY)
                        .weekType(WeekType.EVERY)
                        .startsAt(LocalTime.of(8, 30))
                        .endsAt(LocalTime.of(10, 15))
                        .build()
        ));

        rabbitTemplate.convertAndSend(
                SCHEDULE_TOPIC,
                SCHEDULE_UPDATE_ROUTING_KEY,
                message
        );

        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(courseRepository.findAll()).hasSize(1));
    }
}