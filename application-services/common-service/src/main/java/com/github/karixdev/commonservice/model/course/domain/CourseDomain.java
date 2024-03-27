package com.github.karixdev.commonservice.model.course.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.karixdev.commonservice.serialization.LocalTimeDeserializer;
import com.github.karixdev.commonservice.serialization.LocalTimeSerializer;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Builder
public record CourseDomain(
        @JsonProperty("startsAt")
        @JsonSerialize(using = LocalTimeSerializer.class)
        @JsonDeserialize(using = LocalTimeDeserializer.class)
        LocalTime startsAt,
        @JsonProperty("endsAt")
        @JsonSerialize(using = LocalTimeSerializer.class)
        @JsonDeserialize(using = LocalTimeDeserializer.class)
        LocalTime endsAt,
        @JsonProperty("name")
        String name,
        @JsonProperty("courseType")
        CourseType courseType,
        @JsonProperty("teachers")
        String teachers,
        @JsonProperty("dayOfWeek")
        DayOfWeek dayOfWeek,
        @JsonProperty("weekType")
        WeekType weeks,
        @JsonProperty("classrooms")
        String classrooms,
        @JsonProperty("additionalInfo")
        String additionalInfo
) {
}
