package com.github.karixdev.courseservice.infrastructure.rest.mapper;

import com.github.karixdev.courseservice.application.mapper.ModelMapper;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.infrastructure.rest.payload.request.CourseRequestCourseType;
import org.springframework.stereotype.Component;

@Component
public class CourseRequestCourseTypeToCourseTypeMapper implements ModelMapper<CourseRequestCourseType, CourseType> {

    @Override
    public CourseType map(CourseRequestCourseType input) {
        if (input == null) {
            return null;
        }

        return switch (input) {
            case LECTURE -> CourseType.LECTURE;
            case LAB -> CourseType.LAB;
            case PROJECT -> CourseType.PROJECT;
            case PRACTICAL -> CourseType.PRACTICAL;
            case INFO -> CourseType.INFO;
        };
    }

}
