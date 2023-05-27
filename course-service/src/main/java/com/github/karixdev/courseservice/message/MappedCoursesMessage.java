package com.github.karixdev.courseservice.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.courseservice.dto.BaseCourseDTO;

import java.util.Set;
import java.util.UUID;

public record MappedCoursesMessage(
        @JsonProperty("scheduleId")
        UUID scheduleId,
        @JsonProperty("courses")
        Set<BaseCourseDTO> courses
) {}
