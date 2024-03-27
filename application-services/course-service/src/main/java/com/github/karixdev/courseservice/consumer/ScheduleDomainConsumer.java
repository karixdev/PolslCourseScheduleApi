package com.github.karixdev.courseservice.consumer;

import com.github.karixdev.commonservice.event.schedule.ScheduleDomain;
import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.mapper.CourseMapper;
import com.github.karixdev.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleDomainConsumer {

    private final CourseService courseService;
    private final CourseMapper courseMapper;

    @KafkaListener(topics = "${kafka.topics.schedule-domain}", groupId = "${spring.application.name}-schedule-domain", containerFactory = "scheduleDomainConcurrentKafkaListenerContainerFactory")
    public void consumeScheduleDomain(ConsumerRecord<String, ScheduleDomain> consumerRecord) {
        ScheduleDomain value = consumerRecord.value();

        log.info("Consumed: {}", consumerRecord);

        UUID scheduleId = UUID.fromString(value.scheduleId());
        Set<Course> mappedCourses = value.courses().stream()
                .map(courseMapper::mapToEntity)
                .collect(Collectors.toSet());
        
        mappedCourses.forEach(course -> course.setScheduleId(scheduleId));

        courseService.updateScheduleCourses(scheduleId, mappedCourses);
    }

}
