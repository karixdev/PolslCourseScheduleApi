package com.github.karixdev.polslcoursescheduleapi.schedule.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.polslcoursescheduleapi.course.payload.response.CourseResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class ScheduleWithCoursesResponse extends ScheduleResponse {
    @JsonProperty("courses")
    @JsonIgnoreProperties({"id", "day_of_week"})
    Map<DayOfWeek, List<CourseResponse>> courses;

    public ScheduleWithCoursesResponse(
            Schedule schedule,
            Map<DayOfWeek, List<CourseResponse>> courses
    ) {
        super(schedule);
        this.courses = courses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ScheduleWithCoursesResponse that = (ScheduleWithCoursesResponse) o;
        return Objects.equals(courses, that.courses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), courses);
    }
}
