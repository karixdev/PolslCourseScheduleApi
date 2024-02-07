package com.github.karixdev.courseservice.mapper;

import com.github.karixdev.commonservice.model.course.domain.CourseDomain;
import com.github.karixdev.courseservice.dto.BaseCourseDTO;
import com.github.karixdev.courseservice.dto.CourseResponse;
import com.github.karixdev.courseservice.entity.Course;
import com.github.karixdev.courseservice.entity.CourseType;
import com.github.karixdev.courseservice.entity.WeekType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CourseMapper {
    public CourseResponse map(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .scheduleId(course.getScheduleId())
                .name(course.getName())
                .courseType(course.getCourseType())
                .teachers(course.getTeachers())
                .classrooms(course.getClassroom())
                .additionalInfo(course.getAdditionalInfo())
                .dayOfWeek(course.getDayOfWeek())
                .weekType(course.getWeekType())
                .endsAt(course.getEndsAt())
                .startsAt(course.getStartsAt())
                .build();
    }

    public Course map(BaseCourseDTO courseDTO, UUID scheduleId) {
        return Course.builder()
                .name(courseDTO.getName())
                .courseType(courseDTO.getCourseType())
                .additionalInfo(courseDTO.getAdditionalInfo())
                .dayOfWeek(courseDTO.getDayOfWeek())
                .startsAt(courseDTO.getStartsAt())
                .endsAt(courseDTO.getEndsAt())
                .teachers(courseDTO.getTeachers())
                .classroom(courseDTO.getClassrooms())
                .scheduleId(scheduleId)
                .weekType(courseDTO.getWeekType())
                .build();
    }

    public Course mapToEntity(CourseDomain courseDomain) {
        WeekType weekType = switch (courseDomain.weeks()) {
            case ODD -> WeekType.ODD;
            case EVEN -> WeekType.EVEN;
            case EVERY -> WeekType.EVERY;
        };

        CourseType courseType = switch (courseDomain.courseType()) {
            case PRACTICAL -> CourseType.PRACTICAL;
            case PROJECT -> CourseType.PROJECT;
            case INFO -> CourseType.INFO;
            case LAB -> CourseType.LAB;
            case LECTURE -> CourseType.LECTURE;
        };

        return Course.builder()
                .name(courseDomain.name())
                .courseType(courseType)
                .additionalInfo(courseDomain.additionalInfo())
                .dayOfWeek(courseDomain.dayOfWeek())
                .startsAt(courseDomain.startsAt())
                .endsAt(courseDomain.endsAt())
                .teachers(courseDomain.teachers())
                .classroom(courseDomain.classrooms())
                .weekType(weekType)
                .build();
    }

}