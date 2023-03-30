package com.github.karixdev.scheduleservice.course.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
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
    @JsonProperty("schedule_id")
    private UUID scheduleId;
}
