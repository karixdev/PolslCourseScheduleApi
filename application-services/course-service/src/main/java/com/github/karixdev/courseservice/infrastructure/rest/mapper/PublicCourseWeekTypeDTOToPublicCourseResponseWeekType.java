package com.github.karixdev.courseservice.infrastructure.rest.mapper;

import com.github.karixdev.courseservice.application.dto.user.PublicCourseWeekTypeDTO;
import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.infrastructure.rest.payload.response.PublicCourseResponseWeekType;
import org.springframework.stereotype.Component;

@Component
public class PublicCourseWeekTypeDTOToPublicCourseResponseWeekType implements ModelMapper<PublicCourseWeekTypeDTO, PublicCourseResponseWeekType> {

    @Override
    public PublicCourseResponseWeekType map(PublicCourseWeekTypeDTO input) {
        if (input == null) {
            return null;
        }

        return switch (input) {
            case ODD -> PublicCourseResponseWeekType.ODD;
            case EVEN -> PublicCourseResponseWeekType.EVEN;
            case EVERY -> PublicCourseResponseWeekType.EVERY;
        };
    }

}
