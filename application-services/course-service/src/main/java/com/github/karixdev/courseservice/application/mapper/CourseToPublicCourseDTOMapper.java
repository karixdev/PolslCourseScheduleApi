package com.github.karixdev.courseservice.application.mapper;

import com.github.karixdev.courseservice.application.dto.user.PublicCourseDTO;
import com.github.karixdev.courseservice.application.dto.user.PublicCourseTypeDTO;
import com.github.karixdev.courseservice.application.dto.user.PublicCourseWeekTypeDTO;
import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.entity.CourseType;
import com.github.karixdev.courseservice.domain.entity.WeekType;

public class CourseToPublicCourseDTOMapper implements ModelMapper<Course, PublicCourseDTO> {

    @Override
    public PublicCourseDTO map(Course input) {
        return PublicCourseDTO.builder()
                .scheduleId(input.getScheduleId())
                .name(input.getName())
                .courseType(mapCourseType(input.getCourseType()))
                .teachers(input.getTeachers())
                .additionalInfo(input.getAdditionalInfo())
                .dayOfWeek(input.getDayOfWeek())
                .weekType(mapWeekType(input.getWeekType()))
                .startsAt(input.getStartsAt())
                .endsAt(input.getEndsAt())
                .classrooms(input.getClassrooms())
                .build();
    }

    private PublicCourseTypeDTO mapCourseType(CourseType courseType) {
        return switch (courseType) {
            case LECTURE -> PublicCourseTypeDTO.LECTURE;
            case LAB -> PublicCourseTypeDTO.LAB;
            case PROJECT -> PublicCourseTypeDTO.PROJECT;
            case PRACTICAL -> PublicCourseTypeDTO.PRACTICAL;
            case INFO -> PublicCourseTypeDTO.INFO;
        };
    }

    private PublicCourseWeekTypeDTO mapWeekType(WeekType weekType) {
        return switch (weekType) {
            case ODD -> PublicCourseWeekTypeDTO.ODD;
            case EVEN -> PublicCourseWeekTypeDTO.EVEN;
            case EVERY -> PublicCourseWeekTypeDTO.EVERY;
        };
    }

}
