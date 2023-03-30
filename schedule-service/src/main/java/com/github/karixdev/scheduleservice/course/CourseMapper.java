package com.github.karixdev.scheduleservice.course;

import com.github.karixdev.scheduleservice.course.dto.BaseCourseDTO;
import com.github.karixdev.scheduleservice.course.dto.CourseResponse;
import com.github.karixdev.scheduleservice.schedule.Schedule;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public Course map(BaseCourseDTO courseDTO, Schedule schedule) {
        return Course.builder()
                .name(courseDTO.getName())
                .courseType(courseDTO.getCourseType())
                .additionalInfo(courseDTO.getAdditionalInfo())
                .dayOfWeek(courseDTO.getDayOfWeek())
                .startsAt(courseDTO.getStartsAt())
                .endsAt(courseDTO.getEndsAt())
                .teachers(courseDTO.getTeachers())
                .classroom(courseDTO.getClassrooms())
                .schedule(schedule)
                .weekType(courseDTO.getWeekType())
                .build();
    }

    public CourseResponse map(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .scheduleId(course.getSchedule().getId())
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