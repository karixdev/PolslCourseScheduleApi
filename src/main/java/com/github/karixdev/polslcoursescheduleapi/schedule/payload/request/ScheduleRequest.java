package com.github.karixdev.polslcoursescheduleapi.schedule.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleRequest {
    @JsonProperty("type")
    @NotNull
    @PositiveOrZero
    private Integer type;

    @JsonProperty("plan_polsl_id")
    @NotNull
    @Positive
    private Integer planPolslId;

    @JsonProperty("semester")
    @NotNull
    @Positive
    private Integer semester;

    @JsonProperty("name")
    @NotBlank
    private String name;

    @JsonProperty("group_number")
    @NotNull
    @Positive
    private Integer groupNumber;
}