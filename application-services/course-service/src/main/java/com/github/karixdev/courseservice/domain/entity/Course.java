package com.github.karixdev.courseservice.domain.entity;

import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Course {

    private UUID id;

    private UUID scheduleId;

    private String name;
    private CourseType courseType;
    private String teachers;
    private String classrooms;
    private String additionalInfo;

    private DayOfWeek dayOfWeek;
    private WeekType weekType;

    private LocalTime startsAt;
    private LocalTime endsAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return id != null && Objects.equals(getId(), course.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
