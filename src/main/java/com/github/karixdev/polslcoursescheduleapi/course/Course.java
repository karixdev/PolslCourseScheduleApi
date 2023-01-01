package com.github.karixdev.polslcoursescheduleapi.course;

import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import lombok.*;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "course")
public class Course {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "course_gen"
    )
    @SequenceGenerator(
            name = "course_gen",
            sequenceName = "course_seq",
            allocationSize = 1
    )
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(
            name = "description",
            nullable = false
    )
    String description;

    @Column(
            name = "starts_at",
            nullable = false
    )
    LocalTime startsAt;

    @Column(
            name = "ends_at",
            nullable = false
    )
    LocalTime endsAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "day_of_week",
            nullable = false
    )
    private DayOfWeek dayOfWeek;

    @Enumerated
    @Column(
            name = "weeks",
            nullable = false
    )
    private Weeks weeks;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(
            name = "schedule_id",
            nullable = false,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "course_schedule_id_fk"
            )
    )
    private Schedule schedule;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(id, course.id) &&
                Objects.equals(description, course.description) &&
                Objects.equals(startsAt, course.startsAt) &&
                Objects.equals(endsAt, course.endsAt) &&
                dayOfWeek == course.dayOfWeek &&
                weeks == course.weeks &&
                Objects.equals(schedule.getId(), course.schedule.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, startsAt, endsAt, dayOfWeek, weeks, schedule.getId());
    }
}
