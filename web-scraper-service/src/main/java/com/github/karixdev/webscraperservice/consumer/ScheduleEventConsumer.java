package com.github.karixdev.webscraperservice.consumer;

import com.github.karixdev.webscraperservice.message.ScheduleEventMessage;
import com.github.karixdev.webscraperservice.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.github.karixdev.webscraperservice.props.ScheduleEventMQProperties.*;

@Component
@RequiredArgsConstructor
public class ScheduleEventConsumer {
    private final ScheduleService scheduleService;

    @RabbitListener(queues = {
            SCHEDULE_CREATE_QUEUE,
            SCHEDULE_UPDATE_QUEUE
    })
    private void listenForCourseCreateAndUpdate(ScheduleEventMessage message) {
        scheduleService.handleScheduleCreateAndUpdate(message);
    }
}
