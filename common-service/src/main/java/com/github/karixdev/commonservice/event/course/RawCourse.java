package com.github.karixdev.commonservice.event.course;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.commonservice.model.course.raw.CourseCell;
import com.github.karixdev.commonservice.model.course.raw.TimeCell;
import lombok.Builder;

import java.util.Set;

@Builder
public record RawCourse(
        @JsonProperty("scheduleId")
        String scheduleId,
        @JsonProperty("timeCells")
        Set<TimeCell> timeCells,
        @JsonProperty("courseCells")
        Set<CourseCell> courseCells
) {
}
