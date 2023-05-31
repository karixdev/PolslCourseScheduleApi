package com.github.karixdev.notificationservice.consumer;

import com.github.karixdev.notificationservice.message.NotificationMessage;
import com.github.karixdev.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.github.karixdev.notificationservice.props.NotificationMQProperties.NOTIFICATION_QUEUE;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {
    private final NotificationService notificationService;

    @RabbitListener(queues = NOTIFICATION_QUEUE)
    private void listenForNotificationMessage(NotificationMessage message) {
        notificationService.handleNotificationMessage(message);
    }
}
