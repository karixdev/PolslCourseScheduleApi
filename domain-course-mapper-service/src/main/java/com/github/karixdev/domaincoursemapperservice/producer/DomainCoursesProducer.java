package com.github.karixdev.domaincoursemapperservice.producer;

import com.github.karixdev.domaincoursemapperservice.message.DomainCoursesMessage;
import com.github.karixdev.domaincoursemapperservice.model.domain.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

import static com.github.karixdev.domaincoursemapperservice.props.CoursesMQProperties.*;

@Component
@RequiredArgsConstructor
public class DomainCoursesProducer {
    private final RabbitTemplate rabbitTemplate;

    public void produceDomainCourseMessage(UUID scheduleId, Set<Course> courses) {
        DomainCoursesMessage message = new DomainCoursesMessage(
                scheduleId,
                courses
        );

        System.out.println(message);

        rabbitTemplate.convertAndSend(
                COURSES_EXCHANGE,
                DOMAIN_COURSES_ROUTING_KEY,
                message
        );
    }
}
