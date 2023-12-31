package com.github.karixdev.courseservice.consumer;

import com.github.karixdev.commonservice.event.schedule.ScheduleEvent;
import com.github.karixdev.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleEventConsumer {

    private final CourseService courseService;

    @KafkaListener(topics = "${kafka.topics.schedule-event}", groupId = "${spring.application.name}-schedule-event", containerFactory = "scheduleEventConcurrentKafkaListenerContainerFactory")
    public void consumeScheduleEvent(ConsumerRecord<String, ScheduleEvent> consumerRecord) {
        ScheduleEvent value = consumerRecord.value();
        courseService.handleScheduleEvent(value);
    }

}
