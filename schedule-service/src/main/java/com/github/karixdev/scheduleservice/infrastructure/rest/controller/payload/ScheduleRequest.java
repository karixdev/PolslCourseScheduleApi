package com.github.karixdev.scheduleservice.infrastructure.rest.controller.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

@Builder
public record ScheduleRequest(
        @JsonProperty("type")
        @NotNull
        @PositiveOrZero
        Integer type,
        @JsonProperty("planPolslId")
        @NotNull
        @Positive
        Integer planPolslId,
        @JsonProperty("semester")
        @NotNull
        @Positive
        Integer semester,
        @JsonProperty("major")
        @NotBlank
        String major,
        @JsonProperty("groupNumber")
        @NotNull
        @Positive
        Integer groupNumber,
        @JsonProperty("wd")
        @NotNull
        @PositiveOrZero
        Integer wd
) {}
