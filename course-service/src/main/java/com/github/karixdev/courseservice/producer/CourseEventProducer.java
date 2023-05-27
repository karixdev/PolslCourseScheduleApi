package com.github.karixdev.courseservice.producer;

import com.github.karixdev.courseservice.message.CoursesUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.github.karixdev.courseservice.props.CourseEventMQProperties.COURSES_UPDATE_EXCHANGE;
import static com.github.karixdev.courseservice.props.CourseEventMQProperties.COURSES_UPDATE_ROUTING_KEY;

@Component
@RequiredArgsConstructor
public class CourseEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public void produceCoursesUpdate(UUID scheduleId) {
        CoursesUpdateMessage message = new CoursesUpdateMessage(scheduleId);

        rabbitTemplate.convertAndSend(
                COURSES_UPDATE_EXCHANGE,
                COURSES_UPDATE_ROUTING_KEY,
                message
        );
    }
}
