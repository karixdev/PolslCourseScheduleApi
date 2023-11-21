package com.github.karixdev.domaincoursemapperservice.service;

import com.github.karixdev.commonservice.event.schedule.ScheduleRaw;
import com.github.karixdev.commonservice.model.course.domain.CourseDomain;
import com.github.karixdev.domaincoursemapperservice.exception.NoScheduleStartTimeException;
import com.github.karixdev.domaincoursemapperservice.mapper.CourseCellMapper;
import com.github.karixdev.domaincoursemapperservice.mapper.TimeCellMapper;
import com.github.karixdev.domaincoursemapperservice.producer.ScheduleDomainProducer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseCellMapper mapper;
    private final TimeCellMapper timeCellMapper;
    private final ScheduleDomainProducer producer;

    public void handleScheduleRaw(ConsumerRecord<String, ScheduleRaw> consumerRecord) {
        ScheduleRaw value = consumerRecord.value();

        LocalTime scheduleStartTime = value.timeCells().stream()
                .map(timeCellMapper::mapToLocalTime)
                .min(LocalTime::compareTo)
                .orElseThrow(NoScheduleStartTimeException::new);

        Set<CourseDomain> courses = value.courseCells().stream()
                .map(courseCell -> mapper.mapToCourse(courseCell, scheduleStartTime))
                .collect(Collectors.toSet());

        producer.produceScheduleDomain(consumerRecord.key(), courses);
    }

}
