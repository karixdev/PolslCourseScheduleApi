package com.github.karixdev.domainmodelmapperservice.consumer;

import com.github.karixdev.commonservice.event.schedule.ScheduleRaw;
import com.github.karixdev.domainmodelmapperservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleRawConsumer {

    private final CourseService courseService;

    @KafkaListener(topics = "${kafka.topics.schedule-raw}", groupId = "${spring.application.name}")
    public void consumeScheduleRaw(ConsumerRecord<String, ScheduleRaw> consumerRecord) {
        courseService.handleScheduleRaw(consumerRecord);
    }

}
