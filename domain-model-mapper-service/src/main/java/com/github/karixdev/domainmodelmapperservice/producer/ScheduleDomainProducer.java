package com.github.karixdev.domainmodelmapperservice.producer;

import com.github.karixdev.commonservice.event.schedule.ScheduleDomain;
import com.github.karixdev.commonservice.model.course.domain.CourseDomain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ScheduleDomainProducer {

    private final String topic;
    private final KafkaTemplate<String, ScheduleDomain> kafkaTemplate;

    public ScheduleDomainProducer(
            @Value("${kafka.topics.schedule-domain}") String topic,
            KafkaTemplate<String, ScheduleDomain> kafkaTemplate
    ) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void produceScheduleDomain(String scheduleId, Set<CourseDomain> courses) {
        ScheduleDomain event = ScheduleDomain.builder()
                .scheduleId(scheduleId)
                .courses(courses)
                .build();

        kafkaTemplate.send(topic, scheduleId, event);
    }

}
