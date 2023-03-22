package com.github.karixdev.webscraperservice.course.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record Course(
        @JsonSerialize(using = LocalTimeSerializer.class)
        @JsonDeserialize(using = LocalTimeDeserializer.class)
        @JsonProperty("starts_at")
        LocalTime startsAt,
        @JsonSerialize(using = LocalTimeSerializer.class)
        @JsonDeserialize(using = LocalTimeDeserializer.class)
        @JsonProperty("ends_at")
        LocalTime endsAt,
        @JsonProperty("name")
        String name,
        @JsonProperty("course_type")
        CourseType courseType,
        @JsonProperty("teachers")
        String teachers,
        @JsonProperty("day_of_week")
        DayOfWeek dayOfWeek,
        @JsonProperty("weeks")
        WeekType weeks,
        @JsonProperty("classrooms")
        String classrooms,
        @JsonProperty("additional_info")
        String additionalInfo
) {
}
