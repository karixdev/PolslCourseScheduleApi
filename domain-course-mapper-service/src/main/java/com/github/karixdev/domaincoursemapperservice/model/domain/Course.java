package com.github.karixdev.domaincoursemapperservice.model.domain;

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
        @JsonProperty("startsAt")
        LocalTime startsAt,
        @JsonSerialize(using = LocalTimeSerializer.class)
        @JsonDeserialize(using = LocalTimeDeserializer.class)
        @JsonProperty("endsAt")
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
