package com.github.karixdev.scheduleservice.schedule;

import com.github.karixdev.scheduleservice.ContainersEnvironment;
import com.github.karixdev.scheduleservice.course.CourseRepository;
import com.github.karixdev.scheduleservice.course.CourseType;
import com.github.karixdev.scheduleservice.course.WeekType;
import com.github.karixdev.scheduleservice.course.dto.BaseCourseDTO;
import com.github.karixdev.scheduleservice.schedule.message.ScheduleUpdateResponseMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.github.karixdev.scheduleservice.schedule.props.ScheduleMQProperties.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ScheduleConsumerIT extends ContainersEnvironment {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        rabbitAdmin.purgeQueue(SCHEDULE_UPDATE_RESPONSE_QUEUE, true);
        rabbitAdmin.purgeQueue(SCHEDULE_UPDATE_REQUEST_QUEUE, true);

        scheduleRepository.deleteAll();
    }

    @Test
    void shouldConsumeMessageFromMQAndUpdateScheduleCourses() {
        Schedule schedule = Schedule.builder()
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .wd(0)
                .name("schedule")
                .build();

        scheduleRepository.save(schedule);

        var courseMessage1 = new BaseCourseDTO(
                LocalTime.of(8, 30),
                LocalTime.of(10, 15),
                "Calculus",
                CourseType.LAB,
                "dr Adam, mgr Marcin",
                DayOfWeek.FRIDAY,
                WeekType.EVEN,
                "314 RMS, CEK Room C",
                "contact teacher"
        );

        var courseMessage2 = new BaseCourseDTO(
                LocalTime.of(14, 30),
                LocalTime.of(16, 15),
                "C++",
                CourseType.LECTURE,
                "dr. Henryk",
                DayOfWeek.WEDNESDAY,
                WeekType.EVERY,
                "CEK Room C",
                "contact teacher"
        );

        rabbitTemplate.convertAndSend(
                SCHEDULE_TOPIC,
                SCHEDULE_UPDATE_RESPONSE_ROUTING_KEY,
                new ScheduleUpdateResponseMessage(
                        schedule.getId(),
                        Set.of(courseMessage1, courseMessage2)
                )
        );

        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(courseRepository.findAll())
                        .hasSize(2));
    }
}
