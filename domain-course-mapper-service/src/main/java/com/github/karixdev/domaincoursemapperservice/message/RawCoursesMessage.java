package com.github.karixdev.domaincoursemapperservice.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.domaincoursemapperservice.model.raw.CourseCell;
import com.github.karixdev.domaincoursemapperservice.model.raw.TimeCell;

import java.util.Set;
import java.util.UUID;

public record RawCoursesMessage(
        @JsonProperty("scheduleId")
        UUID scheduleId,
        @JsonProperty("timeCells")
        Set<TimeCell> timeCells,
        @JsonProperty("courseCells")
        Set<CourseCell> courseCells
) {}
