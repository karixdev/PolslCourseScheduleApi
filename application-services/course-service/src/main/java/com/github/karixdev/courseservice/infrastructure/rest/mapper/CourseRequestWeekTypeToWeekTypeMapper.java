package com.github.karixdev.courseservice.infrastructure.rest.mapper;

import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.domain.entity.WeekType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequestWeekType;
import org.springframework.stereotype.Component;

@Component
public class CourseRequestWeekTypeToWeekTypeMapper implements ModelMapper<CourseRequestWeekType, WeekType> {

    @Override
    public WeekType map(CourseRequestWeekType input) {
        if (input == null) {
            return null;
        }

        return switch (input) {
            case ODD -> WeekType.ODD;
            case EVEN -> WeekType.EVEN;
            case EVERY -> WeekType.EVERY;
        };
    }

}
