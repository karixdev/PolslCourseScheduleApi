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
        return Objects.equals(id, course.id) &&
                Objects.equals(scheduleId, course.scheduleId) &&
                Objects.equals(name, course.name) &&
                courseType == course.courseType &&
                Objects.equals(teachers, course.teachers) &&
                Objects.equals(classroom, course.classroom) &&
                Objects.equals(additionalInfo, course.additionalInfo) &&
                dayOfWeek == course.dayOfWeek &&
                weekType == course.weekType &&
                Objects.equals(startsAt, course.startsAt) &&
                Objects.equals(endsAt, course.endsAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                scheduleId,
                name,
                courseType,
                teachers,
                classroom,
                additionalInfo,
                dayOfWeek,
                weekType,
                startsAt,
                endsAt
        );
    }
}