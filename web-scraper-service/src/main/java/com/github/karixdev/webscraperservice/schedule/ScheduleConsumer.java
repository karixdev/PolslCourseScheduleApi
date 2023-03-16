package com.github.karixdev.webscraperservice.schedule;

import com.github.karixdev.webscraperservice.schedule.message.ScheduleUpdateRequestMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.github.karixdev.webscraperservice.schedule.props.ScheduleMQProperties.SCHEDULE_UPDATE_REQUEST_QUEUE;

@Component
@RequiredArgsConstructor
public class ScheduleConsumer {
    private final ScheduleService service;

    @RabbitListener(queues = SCHEDULE_UPDATE_REQUEST_QUEUE)
    private void listenForScheduleUpdatesRequests(ScheduleUpdateRequestMessage message) {
        service.updateSchedule(message);
    }
}
