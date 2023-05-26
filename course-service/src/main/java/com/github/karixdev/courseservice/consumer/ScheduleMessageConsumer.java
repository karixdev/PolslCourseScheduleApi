package com.github.karixdev.courseservice.consumer;

import com.github.karixdev.courseservice.message.ScheduleEventMessage;
import com.github.karixdev.courseservice.props.ScheduleMQProperties;
import com.github.karixdev.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.github.karixdev.courseservice.props.ScheduleMQProperties.SCHEDULE_UPDATE_QUEUE;

@Component
@RequiredArgsConstructor
public class ScheduleMessageConsumer {
    private final CourseService courseService;

    @RabbitListener(queues = SCHEDULE_UPDATE_QUEUE)
    private void listenForScheduleDeletion(ScheduleEventMessage message) {
        courseService.handleScheduleDelete(message.id());
    }
}
