package com.github.karixdev.scheduleservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity
@Table(name = "schedule")
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
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

}
