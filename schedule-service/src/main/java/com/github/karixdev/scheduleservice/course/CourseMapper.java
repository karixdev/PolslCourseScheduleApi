package com.github.karixdev.scheduleservice.course;

import com.github.karixdev.scheduleservice.course.message.CourseMessage;
import com.github.karixdev.scheduleservice.schedule.Schedule;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {
    public Course map(CourseMessage message, Schedule schedule) {
        String teachers = String.join(", ", message.teachers());
        String rooms = String.join(", ", message.rooms());

        return Course.builder()
                .name(message.name())
                .courseType(message.courseType())
                .additionalInfo(message.additionalInfo())
                .dayOfWeek(message.dayOfWeek())
                .startsAt(message.startsAt())
                .endsAt(message.endsAt())
                .teachers(teachers)
                .classroom(rooms)
                .schedule(schedule)
                .weekType(message.weeks())
                .build();
    }
}