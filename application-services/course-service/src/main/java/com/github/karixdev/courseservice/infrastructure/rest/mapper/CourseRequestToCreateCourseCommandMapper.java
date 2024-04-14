package com.github.karixdev.courseservice.infrastructure.rest.mapper;

import com.github.karixdev.courseservice.application.command.CreateCourseCommand;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequest;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequestCourseType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequestWeekType;
import org.springframework.stereotype.Component;

@Component
public class CourseRequestToCreateCourseCommandMapper implements ModelMapper<CourseRequest, CreateCourseCommand> {

    @Override
    public CreateCourseCommand map(CourseRequest input) {
        return CreateCourseCommand.builder()
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

    private CourseType mapCourseType(CourseRequestCourseType courseType) {
        if (courseType == null) {
            return null;
        }

        return switch (courseType) {
            case LECTURE -> CourseType.LECTURE;
            case LAB -> CourseType.LAB;
            case PROJECT -> CourseType.PROJECT;
            case PRACTICAL -> CourseType.PRACTICAL;
            case INFO -> CourseType.INFO;
        };
    }

    private WeekType mapWeekType(CourseRequestWeekType weekType) {
        if (weekType == null) {
            return null;
        }

        return switch (weekType) {
            case ODD -> WeekType.ODD;
            case EVEN -> WeekType.EVEN;
            case EVERY -> WeekType.EVERY;
        };
    }

}
