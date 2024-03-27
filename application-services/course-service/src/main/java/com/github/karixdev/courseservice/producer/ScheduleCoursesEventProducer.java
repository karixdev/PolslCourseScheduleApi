package com.github.karixdev.courseservice.producer;

import com.github.karixdev.commonservice.event.course.ScheduleCoursesEvent;
import com.github.karixdev.courseservice.entity.Course;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ScheduleCoursesEventProducer {

    private final KafkaTemplate<String, ScheduleCoursesEvent> kafkaTemplate;
    private final String topic;

    public ScheduleCoursesEventProducer(
            KafkaTemplate<String, ScheduleCoursesEvent> kafkaTemplate,
            @Value("${kafka.topics.schedule-courses-event}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void produceCreated(UUID scheduleId, Set<Course> created) {
        produceEvent(scheduleId, created, null, null);
    }

    public void produceUpdated(UUID scheduleId, Set<Course> updated) {
        produceEvent(scheduleId, null, updated, null);
    }

    public void produceDeleted(UUID scheduleId, Set<Course> deleted) {
        produceEvent(scheduleId, null, null, deleted);
    }

    public void produceCreatedAndDeleted(UUID scheduleId, Set<Course> created, Set<Course> deleted) {
        produceEvent(scheduleId, created, null, deleted);
    }

    private void produceEvent(UUID scheduleId, Set<Course> created, Set<Course> updated, Set<Course> deleted) {
        if (created == null) {
            created = Set.of();
        }
        if (updated == null) {
            updated = Set.of();
        }
        if (deleted == null) {
            deleted = Set.of();
        }

        Set<String> createdIds = created.stream()
                .map(course -> course.getId().toString())
                .collect(Collectors.toSet());
        Set<String> updatedIds = updated.stream()
                .map(course -> course.getId().toString())
                .collect(Collectors.toSet());
        Set<String> deletedIds = deleted.stream()
                .map(course -> course.getId().toString())
                .collect(Collectors.toSet());

        ScheduleCoursesEvent event = ScheduleCoursesEvent.builder()
                .scheduleId(scheduleId.toString())
                .created(createdIds)
                .updated(updatedIds)
                .deleted(deletedIds)
                .build();

        kafkaTemplate.send(topic, scheduleId.toString(), event);
    }

}
