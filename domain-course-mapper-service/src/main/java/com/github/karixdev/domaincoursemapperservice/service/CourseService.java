package com.github.karixdev.domaincoursemapperservice.service;

import com.github.karixdev.domaincoursemapperservice.exception.NoScheduleStartTimeException;
import com.github.karixdev.domaincoursemapperservice.mapper.CourseCellMapper;
import com.github.karixdev.domaincoursemapperservice.mapper.TimeCellMapper;
import com.github.karixdev.domaincoursemapperservice.model.domain.Course;
import com.github.karixdev.domaincoursemapperservice.model.raw.CourseCell;
import com.github.karixdev.domaincoursemapperservice.model.raw.TimeCell;
import com.github.karixdev.domaincoursemapperservice.producer.DomainCoursesProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseCellMapper mapper;
    private final TimeCellMapper timeCellMapper;
    private final DomainCoursesProducer producer;

    public void handleRawCoursesMessage(UUID scheduleId, Set<TimeCell> timeCells, Set<CourseCell> courseCells) {
        LocalTime scheduleStartTime = timeCells.stream()
                .map(timeCellMapper::mapToLocalTime)
                .min(LocalTime::compareTo)
                .orElseThrow(NoScheduleStartTimeException::new);

        Set<Course> courses = courseCells.stream()
                .map(courseCell -> mapper.mapToCourse(courseCell, scheduleStartTime))
                .collect(Collectors.toSet());

        producer.produceDomainCourseMessage(scheduleId, courses);
    }
}
