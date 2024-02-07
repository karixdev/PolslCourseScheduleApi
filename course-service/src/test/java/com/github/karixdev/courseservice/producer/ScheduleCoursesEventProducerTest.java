package com.github.karixdev.courseservice.producer;

import com.github.karixdev.commonservice.event.course.ScheduleCoursesEvent;
import com.github.karixdev.courseservice.entity.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ScheduleCoursesEventProducerTest {

    ScheduleCoursesEventProducer underTest;

    KafkaTemplate<String, ScheduleCoursesEvent> kafkaTemplate;

    String topic;

    UUID scheduleId;
    Course course1;
    Course course2;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        kafkaTemplate = (KafkaTemplate<String, ScheduleCoursesEvent>) mock(KafkaTemplate.class);
        topic = "topic";
        underTest = new ScheduleCoursesEventProducer(kafkaTemplate, topic);

        scheduleId = UUID.randomUUID();
        course1 = Course.builder().id(UUID.randomUUID()).build();
        course2 = Course.builder().id(UUID.randomUUID()).build();
    }

    @Test
    void GivenScheduleIdAndSetOfCourses_WhenProduceCreated_ThenCorrectEventIsPublishedToCorrectTopic() {
        // Given
        Set<Course> created = Set.of(course1);

        // When
        underTest.produceCreated(scheduleId, created);

        // Then
        ScheduleCoursesEvent event = ScheduleCoursesEvent.builder()
                .scheduleId(scheduleId.toString())
                .created(Set.of(course1.getId().toString()))
                .updated(Set.of())
                .deleted(Set.of())
                .build();

        verify(kafkaTemplate).send(topic, scheduleId.toString(), event);
    }

    @Test
    void GivenScheduleIdAndSetOfCourses_WhenProduceUpdated_ThenCorrectEventIsPublishedToCorrectTopic() {
        // Given
        Set<Course> created = Set.of(course1);

        // When
        underTest.produceUpdated(scheduleId, created);

        // Then
        ScheduleCoursesEvent event = ScheduleCoursesEvent.builder()
                .scheduleId(scheduleId.toString())
                .created(Set.of())
                .updated(Set.of(course1.getId().toString()))
                .deleted(Set.of())
                .build();

        verify(kafkaTemplate).send(topic, scheduleId.toString(), event);
    }

    @Test
    void GivenScheduleIdAndSetOfCourses_WhenProduceDeleted_ThenCorrectEventIsPublishedToCorrectTopic() {
        // Given
        Set<Course> created = Set.of(course1);

        // When
        underTest.produceDeleted(scheduleId, created);

        // Then
        ScheduleCoursesEvent event = ScheduleCoursesEvent.builder()
                .scheduleId(scheduleId.toString())
                .created(Set.of())
                .updated(Set.of())
                .deleted(Set.of(course1.getId().toString()))
                .build();

        verify(kafkaTemplate).send(topic, scheduleId.toString(), event);
    }

    @Test
    void GivenScheduleIdAndSetOfCourses_WhenProduceCreatedAndDeleted_ThenCorrectEventIsPublishedToCorrectTopic() {
        // Given
        Set<Course> created = Set.of(course1);
        Set<Course> deleted = Set.of(course2);

        // When
        underTest.produceCreatedAndDeleted(scheduleId, created, deleted);

        // Then
        ScheduleCoursesEvent event = ScheduleCoursesEvent.builder()
                .scheduleId(scheduleId.toString())
                .created(Set.of(course1.getId().toString()))
                .updated(Set.of())
                .deleted(Set.of(course2.getId().toString()))
                .build();

        verify(kafkaTemplate).send(topic, scheduleId.toString(), event);
    }

}