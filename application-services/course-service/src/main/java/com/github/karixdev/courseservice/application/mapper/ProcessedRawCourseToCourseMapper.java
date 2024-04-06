package com.github.karixdev.courseservice.application.mapper;

import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawCourse;
import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawCourseType;
import com.github.karixdev.courseservice.domain.entity.processed.ProcessedRawCourseWeekType;
import org.springframework.stereotype.Component;

@Component
public class ProcessedRawCourseToCourseMapper implements ModelMapper<ProcessedRawCourse, Course> {

    @Override
    public Course map(ProcessedRawCourse input) {
        return Course.builder()
                .scheduleId(input.scheduleId())
                .name(input.name())
                .courseType(mapCourseType(input.courseType()))
                .teachers(input.teachers())
                .additionalInfo(input.additionalInfo())
                .dayOfWeek(input.dayOfWeek())
                .weekType(mapWeekType(input.weekType()))
                .startsAt(input.startsAt())
                .endsAt(input.endsAt())
                .classrooms(input.classrooms())
                .build();
    }

    private CourseType mapCourseType(ProcessedRawCourseType processedRawCourseType) {
        return switch (processedRawCourseType) {
            case LECTURE -> CourseType.LECTURE;
            case LAB -> CourseType.LAB;
            case PROJECT -> CourseType.PROJECT;
            case PRACTICAL -> CourseType.PRACTICAL;
            case INFO -> CourseType.INFO;
        };
    }

    private WeekType mapWeekType(ProcessedRawCourseWeekType processedRawCourseWeekType) {
        return switch (processedRawCourseWeekType) {
            case ODD -> WeekType.ODD;
            case EVEN -> WeekType.EVEN;
            case EVERY -> WeekType.EVERY;
        };
    }

}
