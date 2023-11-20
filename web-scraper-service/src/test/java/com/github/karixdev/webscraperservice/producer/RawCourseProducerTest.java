package com.github.karixdev.webscraperservice.producer;

import com.github.karixdev.commonservice.event.schedule.RawCourse;
import com.github.karixdev.commonservice.model.course.raw.CourseCell;
import com.github.karixdev.commonservice.model.schedule.raw.TimeCell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RawCourseProducerTest {

    RawCourseProducer underTest;

    KafkaTemplate<String, RawCourse> kafkaTemplate;

    String topic;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        kafkaTemplate = (KafkaTemplate<String, RawCourse>) mock(KafkaTemplate.class);
        topic = "output-topic";
        underTest = new RawCourseProducer(topic, kafkaTemplate);
    }

    @Test
    void GivenScheduleIdCourseCellsTimeCells_WhenProducerRawCoursesMessage_ThenProducesCorrectEventToProperTopic() {
        // Given
        String scheduleId = UUID.randomUUID().toString();
        Set<TimeCell> timeCells = Set.of(
                new TimeCell("08:30-10:00")
        );
        Set<CourseCell> courseCells = Set.of(
                new CourseCell(
                        10,
                        10,
                        10,
                        10,
                        "text"
                )
        );

        // When
        underTest.produceRawCourse(scheduleId, courseCells, timeCells);

        // Then
        RawCourse expectedMessage = new RawCourse(
                scheduleId,
                timeCells,
                courseCells
        );

        verify(kafkaTemplate).send(topic, scheduleId, expectedMessage);
    }

}