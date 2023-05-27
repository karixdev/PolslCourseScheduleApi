package com.github.karixdev.courseservice.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record CoursesUpdateMessage(
        @JsonProperty("scheduleId")
        UUID scheduleId
) {}
