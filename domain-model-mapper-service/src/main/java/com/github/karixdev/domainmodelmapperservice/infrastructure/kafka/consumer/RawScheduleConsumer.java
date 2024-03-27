package com.github.karixdev.domainmodelmapperservice.infrastructure.kafka.consumer;

import com.github.karixdev.domainmodelmapperservice.application.event.RawScheduleEvent;
import com.github.karixdev.domainmodelmapperservice.application.event.handler.EventHandler;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RawScheduleConsumer {

    private final EventHandler<RawScheduleEvent> eventHandler;

    @KafkaListener(topics = "${kafka.topics.schedule-raw}", groupId = "${spring.application.name}", containerFactory = "rawScheduleEventConcurrentKafkaListenerContainerFactory")
    public void consumeScheduleRaw(ConsumerRecord<String, RawScheduleEvent> consumerRecord) {
        eventHandler.handle(consumerRecord.value());
    }

}
