package com.github.karixdev.courseservice.producer;

import com.github.karixdev.courseservice.message.CoursesUpdateMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static com.github.karixdev.courseservice.props.CourseEventMQProperties.COURSES_UPDATE_EXCHANGE;
import static com.github.karixdev.courseservice.props.CourseEventMQProperties.COURSES_UPDATE_ROUTING_KEY;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CourseEventProducerTest {
    @InjectMocks
    CourseEventProducer underTest;

    @Mock
    RabbitTemplate rabbitTemplate;

    @Test
    void GivenScheduleId_WhenProduceCoursesUpdate_ThenProducesCorrectMessageToCorrectQueue() {
        // Given
        UUID scheduleId = UUID.randomUUID();

        CoursesUpdateMessage expectedMessage =
                new CoursesUpdateMessage(scheduleId);

        // When
        underTest.produceCoursesUpdate(scheduleId);

        // Then
        verify(rabbitTemplate).convertAndSend(
                eq(COURSES_UPDATE_EXCHANGE),
                eq(COURSES_UPDATE_ROUTING_KEY),
                eq(expectedMessage)
        );
    }
}