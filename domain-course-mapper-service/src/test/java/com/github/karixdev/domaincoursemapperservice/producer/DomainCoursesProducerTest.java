package com.github.karixdev.domaincoursemapperservice.producer;

import com.github.karixdev.domaincoursemapperservice.message.DomainCoursesMessage;
import com.github.karixdev.domaincoursemapperservice.model.domain.Course;
import com.github.karixdev.domaincoursemapperservice.model.domain.CourseType;
import com.github.karixdev.domaincoursemapperservice.model.domain.WeekType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import static com.github.karixdev.domaincoursemapperservice.props.CoursesMQProperties.COURSES_EXCHANGE;
import static com.github.karixdev.domaincoursemapperservice.props.CoursesMQProperties.DOMAIN_COURSES_ROUTING_KEY;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DomainCoursesProducerTest {
    @InjectMocks
    DomainCoursesProducer underTest;

    @Mock
    RabbitTemplate rabbitTemplate;

    @Test
    void GivenScheduleIdAndCoursesSet_WhenProduceDomainCourseMessage_ThenProducesCorrectMessageToCorrectQueue() {
        // Given
        UUID scheduleId = UUID.randomUUID();
        Set<Course> courses = Set.of(
                new Course(
                        LocalTime.of(8, 30),
                        LocalTime.of(11, 45),
                        "course 1",
                        CourseType.INFO,
                        "",
                        DayOfWeek.TUESDAY,
                        WeekType.EVERY,
                        "",
                        null
                )
        );

        DomainCoursesMessage message = new DomainCoursesMessage(
                scheduleId,
                courses
        );

        // When
        underTest.produceDomainCourseMessage(
                scheduleId,
                courses
        );

        // Then
        verify(rabbitTemplate).convertAndSend(
                eq(COURSES_EXCHANGE),
                eq(DOMAIN_COURSES_ROUTING_KEY),
                eq(message)
        );
    }

}