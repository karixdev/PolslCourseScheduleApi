package com.github.karixdev.courseservice.infrastructure.kafka.consumer;

import com.github.karixdev.courseservice.application.event.ProcessedRawScheduleEvent;
import com.github.karixdev.courseservice.application.event.handler.EventHandler;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcessedRawScheduleEventConsumer {

    private final EventHandler<ProcessedRawScheduleEvent> eventHandler;

    @KafkaListener(
            topics = "${kafka.topics.processed-raw-schedule}",
            groupId = "${spring.application.name}",
            containerFactory = "processedRawScheduleEventConcurrentKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, ProcessedRawScheduleEvent> consumerRecord) {
        eventHandler.handle(consumerRecord.value());
    }

}
