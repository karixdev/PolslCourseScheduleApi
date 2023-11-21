package com.github.karixdev.domaincoursemapperservice.producer;

import com.github.karixdev.commonservice.event.schedule.ScheduleDomain;
import com.github.karixdev.commonservice.model.course.domain.CourseDomain;
import com.github.karixdev.commonservice.model.course.domain.CourseType;
import com.github.karixdev.commonservice.model.course.domain.WeekType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ScheduleDomainProducerTest {

    ScheduleDomainProducer underTest;

    KafkaTemplate<String, ScheduleDomain> kafkaTemplate;
    String topic;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        kafkaTemplate =  (KafkaTemplate<String, ScheduleDomain>) mock(KafkaTemplate.class);
        topic = "topic";
        underTest = new ScheduleDomainProducer(topic, kafkaTemplate);
    }

    @Test
    void GivenScheduleIdAndSetOfCourses_WhenProduceScheduleDomain_ThenProducesCorrectEvent() {
        // Given
        String scheduleId = UUID.randomUUID().toString();
        CourseDomain courseDomain = CourseDomain.builder()
                .startsAt(LocalTime.of(8, 30))
                .endsAt(LocalTime.of(11, 45))
                .name("course 1")
                .courseType(CourseType.INFO)
                .teachers("")
                .dayOfWeek(DayOfWeek.TUESDAY)
                .weeks(WeekType.EVERY)
                .classrooms("")
                .build();

        ScheduleDomain expected = ScheduleDomain.builder()
                .scheduleId(scheduleId)
                .courses(Set.of(courseDomain))
                .build();

        // When
        underTest.produceScheduleDomain(scheduleId, Set.of(courseDomain));

        // Then
        verify(kafkaTemplate).send(topic, scheduleId, expected);
    }

}