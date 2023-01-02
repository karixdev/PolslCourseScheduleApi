package com.github.karixdev.polslcoursescheduleapi.schedule.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleCollectionResponse {
    @JsonIgnoreProperties({"semester"})
    private Map<Integer, List<ScheduleResponse>> semesters =
            new HashMap<>();
}
