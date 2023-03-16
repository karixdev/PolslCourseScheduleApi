package com.github.karixdev.webscraperservice.schedule;

import com.github.karixdev.webscraperservice.course.domain.Course;
import com.github.karixdev.webscraperservice.schedule.message.ScheduleUpdateResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

import static com.github.karixdev.webscraperservice.schedule.props.ScheduleMQProperties.SCHEDULE_TOPIC;
import static com.github.karixdev.webscraperservice.schedule.props.ScheduleMQProperties.SCHEDULE_UPDATE_RESPONSE_ROUTING_KEY;

@Component
@RequiredArgsConstructor
public class ScheduleProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendScheduleUpdateResponseMessage(UUID id, Set<Course> courses) {
        ScheduleUpdateResponseMessage msg =
                new ScheduleUpdateResponseMessage(id, courses);

        rabbitTemplate.convertAndSend(
                SCHEDULE_TOPIC,
                SCHEDULE_UPDATE_RESPONSE_ROUTING_KEY,
                msg
        );
    }
}
