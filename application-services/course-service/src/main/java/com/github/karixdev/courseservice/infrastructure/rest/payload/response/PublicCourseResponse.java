package com.github.karixdev.courseservice.infrastructure.rest.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record PublicCourseResponse(
        @JsonProperty("id")
        UUID id,
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
        PublicCourseResponseCourseType courseType,
        @JsonProperty("teachers")
        @Nullable
        String teachers,
        @JsonProperty("dayOfWeek")
        @NotNull
        DayOfWeek dayOfWeek,
        @JsonProperty("weekType")
        @NotNull
        PublicCourseResponseWeekType weekType,
        @JsonProperty("classrooms")
        @Nullable
        String classrooms,
        @JsonProperty("additionalInfo")
        @Nullable
        String additionalInfo
) {}
