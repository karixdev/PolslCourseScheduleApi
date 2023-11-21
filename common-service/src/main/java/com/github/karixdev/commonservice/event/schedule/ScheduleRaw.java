package com.github.karixdev.commonservice.event.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.commonservice.model.course.raw.CourseCell;
import com.github.karixdev.commonservice.model.schedule.raw.TimeCell;
import lombok.Builder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Builder
public record ScheduleRaw(
        @JsonProperty("scheduleId")
        String scheduleId,
        @JsonProperty("timeCells")
        Set<TimeCell> timeCells,
        @JsonProperty("courseCells")
        Set<CourseCell> courseCells
) {
    public ScheduleRaw(
            @JsonProperty("scheduleId")
            String scheduleId, @JsonProperty("timeCells")
            Set<TimeCell> timeCells, @JsonProperty("courseCells")
            Set<CourseCell> courseCells
    ) {
        this.scheduleId = scheduleId;
        this.timeCells = Objects.requireNonNullElseGet(timeCells, HashSet::new);
        this.courseCells = Objects.requireNonNullElseGet(courseCells, HashSet::new);
    }
}
