package com.github.karixdev.scheduleservice.course;

import com.github.karixdev.scheduleservice.course.dto.BaseCourseDTO;
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
}