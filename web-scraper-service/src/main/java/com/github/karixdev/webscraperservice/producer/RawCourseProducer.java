package com.github.karixdev.webscraperservice.producer;

import com.github.karixdev.commonservice.event.course.RawCourse;
import com.github.karixdev.commonservice.model.course.raw.CourseCell;
import com.github.karixdev.commonservice.model.course.raw.TimeCell;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RawCourseProducer {

    private final String topic;
    private final KafkaTemplate<String, RawCourse> kafkaTemplate;

    public RawCourseProducer(
            @Value("${kafka.topics.course-raw}") String topic,
            KafkaTemplate<String, RawCourse> kafkaTemplate
    ) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void produceRawCourse(String scheduleId, Set<CourseCell> courseCells, Set<TimeCell> timeCells) {
        RawCourse rawCourse = new RawCourse(scheduleId, timeCells, courseCells);
        kafkaTemplate.send(topic, scheduleId, rawCourse);
    }

}
