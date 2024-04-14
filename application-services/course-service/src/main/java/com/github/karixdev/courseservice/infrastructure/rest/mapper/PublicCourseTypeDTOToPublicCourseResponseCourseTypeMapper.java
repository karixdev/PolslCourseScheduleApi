package com.github.karixdev.courseservice.infrastructure.rest.mapper;

import com.github.karixdev.courseservice.application.dto.user.PublicCourseTypeDTO;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.infrastructure.rest.payload.response.PublicCourseResponseCourseType;
import org.springframework.stereotype.Component;

@Component
public class PublicCourseTypeDTOToPublicCourseResponseCourseTypeMapper implements ModelMapper<PublicCourseTypeDTO, PublicCourseResponseCourseType> {

    @Override
    public PublicCourseResponseCourseType map(PublicCourseTypeDTO input) {
        if (input == null) {
            return null;
        }

        return switch (input) {
            case LECTURE -> PublicCourseResponseCourseType.LECTURE;
            case LAB -> PublicCourseResponseCourseType.LAB;
            case PROJECT -> PublicCourseResponseCourseType.PROJECT;
            case PRACTICAL -> PublicCourseResponseCourseType.PRACTICAL;
            case INFO -> PublicCourseResponseCourseType.INFO;
        };
    }

}
