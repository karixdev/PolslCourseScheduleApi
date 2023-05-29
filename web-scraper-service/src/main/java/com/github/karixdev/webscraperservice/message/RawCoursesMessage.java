package com.github.karixdev.webscraperservice.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.webscraperservice.model.CourseCell;
import com.github.karixdev.webscraperservice.model.TimeCell;

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
