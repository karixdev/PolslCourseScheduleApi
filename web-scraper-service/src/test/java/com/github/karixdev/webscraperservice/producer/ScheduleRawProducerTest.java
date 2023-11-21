package com.github.karixdev.webscraperservice.producer;

import com.github.karixdev.commonservice.event.schedule.ScheduleRaw;
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
class ScheduleRawProducerTest {

    ScheduleRawProducer underTest;

    KafkaTemplate<String, ScheduleRaw> kafkaTemplate;

    String topic;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        kafkaTemplate = (KafkaTemplate<String, ScheduleRaw>) mock(KafkaTemplate.class);
        topic = "output-topic";
        underTest = new ScheduleRawProducer(topic, kafkaTemplate);
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
        ScheduleRaw expectedMessage = new ScheduleRaw(
                scheduleId,
                timeCells,
                courseCells
        );

        verify(kafkaTemplate).send(topic, scheduleId, expectedMessage);
    }

}