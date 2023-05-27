package com.github.karixdev.scheduleservice.producer;

import com.github.karixdev.scheduleservice.entity.Schedule;
import com.github.karixdev.scheduleservice.message.ScheduleUpdateRequestMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.github.karixdev.scheduleservice.props.ScheduleMQProperties.SCHEDULE_TOPIC;
import static com.github.karixdev.scheduleservice.props.ScheduleMQProperties.SCHEDULE_UPDATE_REQUEST_ROUTING_KEY;

@Component
@RequiredArgsConstructor
public class ScheduleProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendScheduleUpdateRequest(Schedule schedule) {
        // Using var to make code more readable
        var message = new ScheduleUpdateRequestMessage(
                schedule.getId(),
                schedule.getType(),
                schedule.getPlanPolslId(),
                schedule.getWd()
        );

        rabbitTemplate.convertAndSend(
                SCHEDULE_TOPIC,
                SCHEDULE_UPDATE_REQUEST_ROUTING_KEY,
                message
        );
    }
}
