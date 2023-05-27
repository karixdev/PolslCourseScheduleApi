package com.github.karixdev.scheduleservice.producer;

import com.github.karixdev.scheduleservice.message.ScheduleEventMessage;
import com.github.karixdev.scheduleservice.message.ScheduleEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.github.karixdev.scheduleservice.props.ScheduleMQProperties.*;

@Component
@RequiredArgsConstructor
public class ScheduleEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public void produceScheduleEventMessage(UUID scheduleId, ScheduleEventType eventType) {
        String routingKey = switch (eventType) {
            case CREATE -> SCHEDULE_CREATE_ROUTING_KEY;
            case UPDATE -> SCHEDULE_UPDATE_ROUTING_KEY;
            case DELETE -> SCHEDULE_DELETE_ROUTING_KEY;
        };

        ScheduleEventMessage message = new ScheduleEventMessage(scheduleId);

        rabbitTemplate.convertAndSend(
                SCHEDULE_EXCHANGE,
                routingKey,
                message
        );
    }

}
