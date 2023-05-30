package com.example.discordnotificationservice.consumer;

import com.example.discordnotificationservice.message.CoursesUpdateMessage;
import com.example.discordnotificationservice.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.example.discordnotificationservice.props.CourseEventMQProperties.COURSES_UPDATE_QUEUE;

@Component
@RequiredArgsConstructor
public class CourseEventConsumer {
    private final WebhookService webhookService;

    @RabbitListener(queues = COURSES_UPDATE_QUEUE)
    private void listenForCourseUpdate(CoursesUpdateMessage message) {
        webhookService.handleCourseUpdateMessage(message);
    }
}
