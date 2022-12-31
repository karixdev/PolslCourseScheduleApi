package com.github.karixdev.polslcoursescheduleapi.schedule;

import com.github.karixdev.polslcoursescheduleapi.user.User;
import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "schedule",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = "name",
                        name = "schedule_name_unique"
                )
        }
)
public class Schedule {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "schedule_gen"
    )
    @SequenceGenerator(
            name = "schedule_gen",
            sequenceName = "schedule_seq",
            allocationSize = 1
    )
    @Column(
            name = "id",
            nullable = false
    )
    @Setter(AccessLevel.NONE)
    private Long id;

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

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(
            name = "added_by_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "schedule_added_by_id_fk"
            )
    )
    private User addedBy;


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
                Objects.equals(addedBy.getId(), schedule.addedBy.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, planPolslId, semester, name, groupNumber, addedBy.getId());
    }
}
