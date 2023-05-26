package com.github.karixdev.courseservice.mapper;

import com.github.karixdev.courseservice.dto.CourseResponse;
import com.github.karixdev.courseservice.entity.Course;
import org.springframework.stereotype.Component;

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
}