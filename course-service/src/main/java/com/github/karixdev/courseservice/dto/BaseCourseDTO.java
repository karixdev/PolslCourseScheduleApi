package com.github.karixdev.courseservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.courseservice.entity.CourseType;
import com.github.karixdev.courseservice.entity.WeekType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BaseCourseDTO {
        @JsonProperty("starts_at")
        @NotNull
        private LocalTime startsAt;
        @JsonProperty("ends_at")
        @NotNull
        private LocalTime endsAt;
        @JsonProperty("name")
        @NotNull
        private String name;
        @JsonProperty("course_type")
        @NotNull
        private CourseType courseType;
        @JsonProperty("teachers")
        @Nullable
        private String teachers;
        @JsonProperty("day_of_week")
        @NotNull
        private DayOfWeek dayOfWeek;
        @JsonProperty("week_type")
        @NotNull
        private WeekType weekType;
        @JsonProperty("classrooms")
        @Nullable
        private String classrooms;
        @JsonProperty("additional_info")
        @Nullable
        private String additionalInfo;
}
