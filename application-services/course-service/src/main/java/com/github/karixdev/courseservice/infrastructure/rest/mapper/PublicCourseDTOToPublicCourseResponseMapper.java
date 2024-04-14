package com.github.karixdev.courseservice.infrastructure.rest.mapper;

import com.github.karixdev.courseservice.application.dto.user.PublicCourseDTO;
import com.github.karixdev.courseservice.application.dto.user.PublicCourseTypeDTO;
import com.github.karixdev.courseservice.application.dto.user.PublicCourseWeekTypeDTO;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.infrastructure.rest.payload.response.PublicCourseResponse;
import com.github.karixdev.courseservice.infrastructure.rest.payload.response.PublicCourseResponseCourseType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.response.PublicCourseResponseWeekType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublicCourseDTOToPublicCourseResponseMapper implements ModelMapper<PublicCourseDTO, PublicCourseResponse> {

    private final ModelMapper<PublicCourseWeekTypeDTO, PublicCourseResponseWeekType> weekTypeMapper;
    private final ModelMapper<PublicCourseTypeDTO, PublicCourseResponseCourseType> courseTypeMapper;

    @Override
    public PublicCourseResponse map(PublicCourseDTO input) {
        return PublicCourseResponse.builder()
                .id(input.id())
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
