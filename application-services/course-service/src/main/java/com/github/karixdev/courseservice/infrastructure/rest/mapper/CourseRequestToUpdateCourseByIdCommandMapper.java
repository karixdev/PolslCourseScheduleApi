package com.github.karixdev.courseservice.infrastructure.rest.mapper;

import com.github.karixdev.courseservice.application.command.UpdateCourseByIdCommand;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.application.mapper.ModelMapperWithAttrs;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequest;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequestCourseType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequestWeekType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CourseRequestToUpdateCourseByIdCommandMapper implements ModelMapperWithAttrs<CourseRequest, UpdateCourseByIdCommand> {

    private final ModelMapper<CourseRequestCourseType, CourseType> courseTypeMapper;
    private final ModelMapper<CourseRequestWeekType, WeekType> weekTypeMapper;

    @Override
    public UpdateCourseByIdCommand map(CourseRequest input, Map<String, Object> attrs) {
        UUID id = (UUID) attrs.get("id");

        return UpdateCourseByIdCommand.builder()
                .id(id)
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
