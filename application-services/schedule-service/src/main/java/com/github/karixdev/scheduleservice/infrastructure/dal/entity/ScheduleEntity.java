package com.github.karixdev.scheduleservice.infrastructure.dal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Entity(name = "Schedule")
@Table(
        name = "schedule",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_plan_polsl_id",
                        columnNames = "plan_polsl_id"
                )
        }
)
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleEntity {

    @Id
    private UUID id;

    @Column(
            name = "type",
            nullable = false
    )
    private Integer type;

    @Column(
            name = "plan_polsl_id",
            nullable = false,
            unique = true
    )
    private Integer planPolslId;

    @Column(
            name = "semester",
            nullable = false
    )
    private Integer semester;

    @Column(
            name = "major",
            nullable = false
    )
    private String major;

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
        ScheduleEntity that = (ScheduleEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
