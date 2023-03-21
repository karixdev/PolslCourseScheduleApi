package com.github.karixdev.scheduleservice.course.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.github.karixdev.scheduleservice.course.CourseType;
import com.github.karixdev.scheduleservice.course.WeekType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public record CourseMessage(
        @JsonSerialize(using = LocalTimeSerializer.class)
        @JsonDeserialize(using = LocalTimeDeserializer.class)
        LocalTime startsAt,
        @JsonSerialize(using = LocalTimeSerializer.class)
        @JsonDeserialize(using = LocalTimeDeserializer.class)
        LocalTime endsAt,
        String name,
        CourseType courseType,
        Set<String> teachers,
        DayOfWeek dayOfWeek,
        WeekType weeks,
        Set<String> rooms,
        String additionalInfo
) {}
