package com.github.karixdev.courseservice.entity;

import jakarta.persistence.*;
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
@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(
            name = "schedule_id",
            nullable = false
    )
    private UUID scheduleId;

    @Column(
            name = "name",
            nullable = false
    )
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "course_type",
            nullable = false
    )
    private CourseType courseType;

    @Column(
            name = "teachers"
    )
    private String teachers;

    @Column(
            name = "classroom"
    )
    private String classroom;

    @Column(
            name = "additional_info"
    )
    private String additionalInfo;

    @Column(
            name = "day_of_week",
            nullable = false
    )
    private DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "week_type",
            nullable = false
    )
    private WeekType weekType;

    @Column(
            name = "starts_at",
            nullable = false
    )
    private LocalTime startsAt;

    @Column(
            name = "ends_at",
            nullable = false
    )
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
