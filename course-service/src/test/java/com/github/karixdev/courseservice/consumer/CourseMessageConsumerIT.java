package com.github.karixdev.courseservice.consumer;

import com.github.karixdev.courseservice.ContainersEnvironment;
import com.github.karixdev.courseservice.dto.BaseCourseDTO;
import com.github.karixdev.courseservice.entity.CourseType;
import com.github.karixdev.courseservice.entity.WeekType;
import com.github.karixdev.courseservice.message.MappedCoursesMessage;
import com.github.karixdev.courseservice.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.karixdev.courseservice.props.MappedCoursesMQProperties.*;
import static com.github.karixdev.courseservice.props.ScheduleMQProperties.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class CourseMessageConsumerIT extends ContainersEnvironment {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        rabbitAdmin.purgeQueue(SCHEDULE_UPDATE_QUEUE, true);
        rabbitAdmin.purgeQueue(DOMAIN_COURSE_QUEUE, true);

        courseRepository.deleteAll();
    }

    @Test
    void shouldConsumeMessageFromMQAndUpdateScheduleCourses() {
        UUID scheduleId = UUID.randomUUID();

        BaseCourseDTO courseMessage1 = new BaseCourseDTO(
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

        BaseCourseDTO courseMessage2 = new BaseCourseDTO(
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
                DOMAIN_COURSE_EXCHANGE,
                DOMAIN_COURSE_ROUTING_KEY,
                new MappedCoursesMessage(
                        scheduleId,
                        Set.of(courseMessage1, courseMessage2)
                )
        );

        await().atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(courseRepository.findAll())
                        .hasSize(2));
    }
}