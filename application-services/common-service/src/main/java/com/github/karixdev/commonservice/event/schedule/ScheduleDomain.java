package com.github.karixdev.commonservice.event.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.commonservice.model.course.domain.CourseDomain;
import lombok.Builder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Builder
public record ScheduleDomain(
        @JsonProperty("scheduleId")
        String scheduleId,
        @JsonProperty("courses")
        Set<CourseDomain> courses
) {
    public ScheduleDomain(
            @JsonProperty("scheduleId")
            String scheduleId, @JsonProperty("courses")
            Set<CourseDomain> courses
    ) {
        this.scheduleId = scheduleId;
        this.courses = Objects.requireNonNullElseGet(courses, HashSet::new);
    }
}
