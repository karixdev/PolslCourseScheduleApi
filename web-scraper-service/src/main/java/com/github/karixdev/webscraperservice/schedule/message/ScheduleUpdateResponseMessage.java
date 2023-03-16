package com.github.karixdev.webscraperservice.schedule.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.webscraperservice.course.domain.Course;

import java.util.Set;
import java.util.UUID;

public record ScheduleUpdateResponseMessage(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("courses")
        Set<Course> courses
) {}
