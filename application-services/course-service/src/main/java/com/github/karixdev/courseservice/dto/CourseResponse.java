package com.github.karixdev.courseservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponse extends BaseCourseDTO {
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("scheduleId")
    private UUID scheduleId;
}
