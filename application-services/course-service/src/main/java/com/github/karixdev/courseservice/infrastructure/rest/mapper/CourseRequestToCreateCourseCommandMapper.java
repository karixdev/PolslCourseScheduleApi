package com.github.karixdev.courseservice.infrastructure.rest.mapper;

import com.github.karixdev.courseservice.application.command.CreateCourseCommand;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequest;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequestCourseType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequestWeekType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseRequestToCreateCourseCommandMapper implements ModelMapper<CourseRequest, CreateCourseCommand> {

    private final ModelMapper<CourseRequestCourseType, CourseType> courseTypeMapper;
    private final ModelMapper<CourseRequestWeekType, WeekType> weekTypeMapper;

    @Override
    public CreateCourseCommand map(CourseRequest input) {
        return CreateCourseCommand.builder()
                .scheduleId(input.scheduleId())
                .name(input.name())
                .courseType(courseTypeMapper.map(input.courseType()))
                .teachers(input.teachers())
                .additionalInfo(input.additionalInfo())
                .dayOfWeek(input.dayOfWeek())
                .weekType(weekTypeMapper.map(input.weekType()))
                .startsAt(input.startsAt())
                .endsAt(input.endsAt())
                .classrooms(input.classrooms())
                .build();
    }

}
