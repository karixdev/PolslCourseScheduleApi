package com.github.karixdev.scheduleservice.course;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Comparator;

@Component
public class CourseComparator implements Comparator<Course> {
    @Override
    public int compare(Course course1, Course course2) {
        DayOfWeek dayOfWeek1 = course1.getDayOfWeek();
        DayOfWeek dayOfWeek2 = course2.getDayOfWeek();

        int dayOfWeekComparisonResult = dayOfWeek1.compareTo(dayOfWeek2);

        if (dayOfWeekComparisonResult != 0) {
            return dayOfWeekComparisonResult;
        }

        LocalTime localTime1 = course1.getStartsAt();
        LocalTime localTime2 = course2.getStartsAt();

        return localTime1.compareTo(localTime2);
    }
}
