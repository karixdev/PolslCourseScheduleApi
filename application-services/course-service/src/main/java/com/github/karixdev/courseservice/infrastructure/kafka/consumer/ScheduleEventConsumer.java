package com.github.karixdev.courseservice.infrastructure.kafka.consumer;

import com.github.karixdev.courseservice.application.event.ScheduleEvent;
import com.github.karixdev.courseservice.application.event.handler.EventHandler;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleEventConsumer {

    private final EventHandler<ScheduleEvent> eventHandler;

    @KafkaListener(
            topics = "${kafka.topics.schedule-event}",
            groupId = "${spring.application.name}",
            containerFactory = "scheduleEventConcurrentKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, ScheduleEvent> consumerRecord) {
        eventHandler.handle(consumerRecord.value());
    }

}
