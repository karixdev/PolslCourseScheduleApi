package com.github.karixdev.polslcoursescheduleapi.planpolsl.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanPolslResponse {
    List<TimeCell> timeCells;
    List<CourseCell> courseCells;
}
