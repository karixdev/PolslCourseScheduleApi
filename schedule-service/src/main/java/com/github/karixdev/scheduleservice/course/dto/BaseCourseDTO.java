package com.github.karixdev.scheduleservice.course.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.karixdev.scheduleservice.course.CourseType;
import com.github.karixdev.scheduleservice.course.WeekType;
import com.github.karixdev.scheduleservice.shared.mapping.LocalTimeDeserializer;
import com.github.karixdev.scheduleservice.shared.mapping.LocalTimeSerializer;
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
        @JsonSerialize(using = LocalTimeSerializer.class)
        @JsonDeserialize(using = LocalTimeDeserializer.class)
        @JsonProperty("starts_at")
        @NotNull
        private LocalTime startsAt;
        @JsonDeserialize(using = LocalTimeDeserializer.class)
        @JsonSerialize(using = LocalTimeSerializer.class)
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
