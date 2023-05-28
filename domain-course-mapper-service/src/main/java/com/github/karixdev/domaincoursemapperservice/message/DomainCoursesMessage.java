package com.github.karixdev.domaincoursemapperservice.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.domaincoursemapperservice.model.domain.Course;

import java.util.Set;
import java.util.UUID;

public record DomainCoursesMessage(
        @JsonProperty("scheduleId")
        UUID scheduleId,
        @JsonProperty("courses")
        Set<Course> courses
) {}
