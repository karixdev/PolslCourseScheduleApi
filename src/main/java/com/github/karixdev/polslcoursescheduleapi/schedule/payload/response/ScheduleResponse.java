package com.github.karixdev.polslcoursescheduleapi.schedule.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("semester")
    private Integer semester;

    @JsonProperty("name")
    private String name;

    @JsonProperty("group_number")
    private Integer groupNumber;

    public ScheduleResponse(Schedule schedule) {
        this.id = schedule.getId();
        this.semester = schedule.getSemester();
        this.name = schedule.getName();
        this.groupNumber = schedule.getGroupNumber();
    }
}
