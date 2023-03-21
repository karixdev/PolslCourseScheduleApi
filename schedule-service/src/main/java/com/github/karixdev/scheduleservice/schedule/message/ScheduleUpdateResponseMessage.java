package com.github.karixdev.scheduleservice.schedule.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.scheduleservice.course.Course;
import com.github.karixdev.scheduleservice.course.message.CourseMessage;

import java.util.Set;
import java.util.UUID;

public record ScheduleUpdateResponseMessage(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("courses")
        Set<CourseMessage> courses
) {}
