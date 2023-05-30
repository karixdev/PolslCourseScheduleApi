package com.github.karixdev.discordnotificationservice.consumer;

import com.github.karixdev.discordnotificationservice.message.CoursesUpdateMessage;
import com.github.karixdev.discordnotificationservice.service.WebhookService;
import com.github.karixdev.discordnotificationservice.props.CourseEventMQProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseEventConsumer {
    private final WebhookService webhookService;

    @RabbitListener(queues = CourseEventMQProperties.COURSES_UPDATE_QUEUE)
    private void listenForCourseUpdate(CoursesUpdateMessage message) {
        webhookService.handleCourseUpdateMessage(message);
    }
}
