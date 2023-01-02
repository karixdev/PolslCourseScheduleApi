package com.github.karixdev.polslcoursescheduleapi.course.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.polslcoursescheduleapi.course.Course;
import com.github.karixdev.polslcoursescheduleapi.course.Weeks;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponse {
    @JsonProperty("id")
    Long id;

    @JsonProperty("description")
    String description;

    @JsonProperty("starts_at")
    LocalTime startsAt;

    @JsonProperty("ends_at")
    LocalTime endsAt;

    @JsonProperty("day_of_week")
    DayOfWeek dayOfWeek;

    @JsonProperty("weeks")
    Weeks weeks;

    public CourseResponse(Course course) {
        this.id = course.getId();
        this.description = course.getDescription();
        this.startsAt = course.getStartsAt();
        this.endsAt = course.getEndsAt();
        this.dayOfWeek = course.getDayOfWeek();
        this.weeks = course.getWeeks();
    }
}
