package com.github.karixdev.courseservice.application.event.handler;

import com.github.karixdev.courseservice.application.event.ProcessedRawScheduleEvent;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.application.updater.ScheduleCoursesUpdater;
import com.github.karixdev.courseservice.application.validator.Validator;
import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawCourse;
import com.github.karixdev.courseservice.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProcessedRawScheduleEventHandler implements EventHandler<ProcessedRawScheduleEvent> {

    private final ModelMapper<ProcessedRawCourse, Course> toEntityMapper;
    private final CourseRepository repository;
    private final ScheduleCoursesUpdater updater;
    private final Validator<ProcessedRawCourse> processedRawCourseValidator;

    @Override
    public void handle(ProcessedRawScheduleEvent event) {
        Set<Course> receivedCourses = event.entity()
                .courses()
                .stream()
                .filter(processedRawCourseValidator::isValid)
                .map(toEntityMapper::map)
                .collect(Collectors.toSet());

        UUID scheduleId = UUID.fromString(event.scheduleId());
        Set<Course> currentCourses = new HashSet<>(repository.findByScheduleId(scheduleId));

        updater.update(currentCourses, receivedCourses);
    }

}
