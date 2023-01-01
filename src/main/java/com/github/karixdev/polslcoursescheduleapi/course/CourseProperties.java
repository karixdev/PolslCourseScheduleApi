package com.github.karixdev.polslcoursescheduleapi.course;

import lombok.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

@Value
@Service
public class CourseProperties {
    int firstCellTopValue = 237;
    double oneHourBlockHeight = 45;
    int courseCellBorderSize = 6;

    int everyWeekCwValue = 154;
    int weekCellHalfOfWidth = 83;

    Map<Integer, DayOfWeek> getDayOfWeekIntegerMap() {
        Map<Integer, DayOfWeek> hashmap = new HashMap<>();

        hashmap.put(88, DayOfWeek.MONDAY);
        hashmap.put(254, DayOfWeek.TUESDAY);
        hashmap.put(420, DayOfWeek.WEDNESDAY);
        hashmap.put(586, DayOfWeek.THURSDAY);
        hashmap.put(752, DayOfWeek.FRIDAY);
        hashmap.put(918, DayOfWeek.SATURDAY);

        return hashmap;
    }
}
