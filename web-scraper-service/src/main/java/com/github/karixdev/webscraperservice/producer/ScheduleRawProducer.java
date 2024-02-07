package com.github.karixdev.webscraperservice.producer;

import com.github.karixdev.commonservice.event.schedule.ScheduleRaw;
import com.github.karixdev.commonservice.model.course.raw.CourseCell;
import com.github.karixdev.commonservice.model.schedule.raw.TimeCell;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ScheduleRawProducer {

    private final String topic;
    private final KafkaTemplate<String, ScheduleRaw> kafkaTemplate;

    public ScheduleRawProducer(
            @Value("${kafka.topics.course-raw}") String topic,
            KafkaTemplate<String, ScheduleRaw> kafkaTemplate
    ) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void produceRawCourse(String scheduleId, Set<CourseCell> courseCells, Set<TimeCell> timeCells) {
        ScheduleRaw rawCourse = new ScheduleRaw(scheduleId, timeCells, courseCells);
        kafkaTemplate.send(topic, scheduleId, rawCourse);
    }

}
