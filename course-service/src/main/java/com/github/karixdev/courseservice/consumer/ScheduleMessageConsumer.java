package com.github.karixdev.courseservice.consumer;

import com.github.karixdev.courseservice.message.ScheduleEventMessage;
import com.github.karixdev.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleMessageConsumer {
    private final CourseService courseService;

    @RabbitListener(queues = "schedule.delete")
    private void listenForScheduleDeletion(ScheduleEventMessage message) {
        courseService.handleScheduleDelete(message.id());
    }
}
