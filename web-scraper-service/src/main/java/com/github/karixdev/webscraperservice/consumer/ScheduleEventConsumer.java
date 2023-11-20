package com.github.karixdev.webscraperservice.consumer;

import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import com.github.karixdev.webscraperservice.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleEventConsumer {

    private final ScheduleService service;

    @KafkaListener(topics = "${kafka.topics.schedule-event}", groupId = "${spring.application.name}")
    public void consumeScheduleEvent(ConsumerRecord<String, ScheduleEvent> consumerRecord) {
        service.handleScheduleEvent(consumerRecord);
    }

}
