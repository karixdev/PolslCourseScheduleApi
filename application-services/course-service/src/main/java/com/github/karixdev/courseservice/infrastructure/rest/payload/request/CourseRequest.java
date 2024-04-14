package com.github.karixdev.courseservice.infrastructure.rest.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record CourseRequest(
        @JsonProperty("scheduleId")
        UUID scheduleId,
        @JsonProperty("startsAt")
        @NotNull
        LocalTime startsAt,
        @JsonProperty("endsAt")
        @NotNull
        LocalTime endsAt,
        @JsonProperty("name")
        @NotNull
        String name,
        @JsonProperty("courseType")
        @NotNull
        CourseRequestCourseType courseType,
        @JsonProperty("teachers")
        @Nullable
        String teachers,
        @JsonProperty("dayOfWeek")
        @NotNull
        DayOfWeek dayOfWeek,
        @JsonProperty("weekType")
        @NotNull
        CourseRequestWeekType weekType,
        @JsonProperty("classrooms")
        @Nullable
        String classrooms,
        @JsonProperty("additionalInfo")
        @Nullable
        String additionalInfo
) {}
