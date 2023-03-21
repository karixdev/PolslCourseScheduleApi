package com.github.karixdev.scheduleservice.course;

import com.github.karixdev.scheduleservice.schedule.Schedule;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "schedule_id",
            nullable = false
    )
    private Schedule schedule;

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
            name = "teachers",
            nullable = false
    )
    private String teachers;

    @Column(
            name = "classroom",
            nullable = false
    )
    private String classroom;

    @Column(
            name = "additional_info"
    )
    @Builder.Default
    private String additionalInfo = "";

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
                Objects.equals(schedule, course.schedule) &&
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
                schedule,
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
