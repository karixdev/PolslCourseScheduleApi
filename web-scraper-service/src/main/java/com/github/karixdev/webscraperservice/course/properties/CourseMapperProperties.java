package com.github.karixdev.webscraperservice.course.properties;

import java.time.DayOfWeek;
import java.util.Map;

public class CourseMapperProperties {
    public static final int FIRST_CELL_TOP_VALUE = 237;
    public static final double ONE_HOUR_CELL_HEIGHT = 45.0;
    public static final int COURSE_CELL_BORDER_SIZE = 6;
    public static final int WEEK_CELL_HALF_OF_WIDTH = 83;
    public static final int EVERY_WEEK_CW_VALUE = 154;
    public static final Map<Integer, DayOfWeek> DAY_OF_WEEK_MAP = Map.of(
            88, DayOfWeek.MONDAY,
            254, DayOfWeek.TUESDAY,
            420, DayOfWeek.WEDNESDAY,
            586, DayOfWeek.THURSDAY,
            752, DayOfWeek.FRIDAY,
            918, DayOfWeek.SATURDAY
    );
}
