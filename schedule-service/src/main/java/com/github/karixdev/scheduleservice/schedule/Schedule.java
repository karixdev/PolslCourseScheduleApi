package com.github.karixdev.scheduleservice.schedule;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "schedule")
public class Schedule {
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
            name = "type",
            nullable = false
    )
    private Integer type;

    @Column(
            name = "plan_polsl_id",
            nullable = false
    )
    private Integer planPolslId;

    @Column(
            name = "semester",
            nullable = false
    )
    private Integer semester;

    @Column(
            name = "name",
            nullable = false
    )
    private String name;

    @Column(
            name = "group_number",
            nullable = false
    )
    private Integer groupNumber;

    @Column(
            name = "wd",
            nullable = false
    )
    private Integer wd;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return Objects.equals(id, schedule.id) &&
                Objects.equals(type, schedule.type) &&
                Objects.equals(planPolslId, schedule.planPolslId) &&
                Objects.equals(semester, schedule.semester) &&
                Objects.equals(name, schedule.name) &&
                Objects.equals(groupNumber, schedule.groupNumber) &&
                Objects.equals(wd, schedule.wd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, planPolslId, semester, name, groupNumber, wd);
    }
}
