package com.github.karixdev.scheduleservice.schedule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ScheduleRequest(
        @JsonProperty("type")
        @NotNull
        @PositiveOrZero
        Integer type,
        @JsonProperty("plan_polsl_id")
        @NotNull
        @Positive
        Integer planPolslId,
        @JsonProperty("semester")
        @NotNull
        @Positive
        Integer semester,
        @JsonProperty("name")
        @NotBlank
        String name,
        @JsonProperty("group_number")
        @NotNull
        @Positive
        Integer groupNumber
) {}
