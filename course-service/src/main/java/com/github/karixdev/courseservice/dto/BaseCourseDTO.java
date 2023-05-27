package com.github.karixdev.courseservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.karixdev.courseservice.dto.serialization.LocalTimeDeserializer;
import com.github.karixdev.courseservice.dto.serialization.LocalTimeSerializer;
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
        @JsonProperty("startsAt")
        @JsonSerialize(using = LocalTimeSerializer.class)
        @JsonDeserialize(using = LocalTimeDeserializer.class)
        @NotNull
        private LocalTime startsAt;
        @JsonProperty("endsAt")
        @JsonSerialize(using = LocalTimeSerializer.class)
        @JsonDeserialize(using = LocalTimeDeserializer.class)
        @NotNull
        private LocalTime endsAt;
        @JsonProperty("name")
        @NotNull
        private String name;
        @JsonProperty("courseType")
        @NotNull
        private CourseType courseType;
        @JsonProperty("teachers")
        @Nullable
        private String teachers;
        @JsonProperty("dayOfWeek")
        @NotNull
        private DayOfWeek dayOfWeek;
        @JsonProperty("weekType")
        @NotNull
        private WeekType weekType;
        @JsonProperty("classrooms")
        @Nullable
        private String classrooms;
        @JsonProperty("additionalInfo")
        @Nullable
        private String additionalInfo;
}
