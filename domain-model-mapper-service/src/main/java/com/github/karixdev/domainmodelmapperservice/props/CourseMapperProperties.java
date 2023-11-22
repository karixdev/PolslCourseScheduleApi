package com.github.karixdev.domainmodelmapperservice.props;

import com.github.karixdev.commonservice.model.course.domain.CourseType;
import lombok.experimental.UtilityClass;

import java.time.DayOfWeek;
import java.util.Map;

@UtilityClass
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
    public static final String COURSE_LINKS_PREFIX = "plan.php";
    public static final String COURSE_ADDITIONAL_INFO_PREFIX = "występowanie";
    public static final String COURSE_LINK_TEACHER_TYPE = "10";
    public static final String COURSE_LINK_ROOM_TYPE = "20";
    public static final Map<String, CourseType> COURSE_TYPE_MAP = Map.of(
            "ćw", CourseType.PRACTICAL,
            "lab", CourseType.LAB,
            "proj", CourseType.PROJECT,
            "wyk", CourseType.LECTURE
    );

}
