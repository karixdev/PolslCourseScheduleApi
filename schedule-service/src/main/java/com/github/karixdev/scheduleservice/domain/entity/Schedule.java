package com.github.karixdev.scheduleservice.domain.entity;

import lombok.*;

import java.util.Objects;
import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    private UUID id;

    private Integer semester;
    private String name;
    private Integer groupNumber;

    private Integer type;
    private Integer planPolslId;
    private Integer wd;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return id != null && Objects.equals(id, schedule.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
