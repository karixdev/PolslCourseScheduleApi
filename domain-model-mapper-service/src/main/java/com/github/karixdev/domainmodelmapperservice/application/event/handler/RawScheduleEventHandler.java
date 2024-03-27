package com.github.karixdev.domainmodelmapperservice.application.event.handler;

import com.github.karixdev.domainmodelmapperservice.application.event.ProcessedRawScheduleEvent;
import com.github.karixdev.domainmodelmapperservice.application.event.RawScheduleEvent;
import com.github.karixdev.domainmodelmapperservice.application.event.producer.EventProducer;
import com.github.karixdev.domainmodelmapperservice.application.exception.EmptyProcessedRawCourseSetException;
import com.github.karixdev.domainmodelmapperservice.application.mapper.ProcessedRawCourseMapper;
import com.github.karixdev.domainmodelmapperservice.application.mapper.ProcessedRawTimeIntervalMapper;
import com.github.karixdev.domainmodelmapperservice.domain.processed.ProcessedRawCourse;
import com.github.karixdev.domainmodelmapperservice.domain.processed.ProcessedRawSchedule;
import com.github.karixdev.domainmodelmapperservice.domain.processed.ProcessedRawTimeInterval;
import com.github.karixdev.domainmodelmapperservice.domain.raw.RawSchedule;
import com.github.karixdev.domainmodelmapperservice.application.exception.NoScheduleStartTimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RawScheduleEventHandler implements EventHandler<RawScheduleEvent> {

    private final ProcessedRawCourseMapper courseMapper;
    private final ProcessedRawTimeIntervalMapper timeIntervalMapper;
    private final EventProducer<ProcessedRawScheduleEvent> eventProducer;

    @Override
    public void handle(RawScheduleEvent event) {
        RawSchedule rawSchedule = event.entity();

        LocalTime scheduleStartTime = rawSchedule.timeIntervals().stream()
                .map(timeIntervalMapper::map)
                .map(ProcessedRawTimeInterval::start)
                .min(LocalTime::compareTo)
                .orElseThrow(NoScheduleStartTimeException::new);

        Set<ProcessedRawCourse> courses = rawSchedule.courses().stream()
                .map(course -> courseMapper.map(course, scheduleStartTime))
                .collect(Collectors.toSet());

        if (courses.isEmpty()) {
            throw new EmptyProcessedRawCourseSetException();
        }

        ProcessedRawSchedule schedule = new ProcessedRawSchedule(courses);

        ProcessedRawScheduleEvent processedRawScheduleEvent = ProcessedRawScheduleEvent.builder()
                .scheduleId(event.scheduleId())
                .entity(schedule)
                .build();

        eventProducer.produce(processedRawScheduleEvent);
    }

}
