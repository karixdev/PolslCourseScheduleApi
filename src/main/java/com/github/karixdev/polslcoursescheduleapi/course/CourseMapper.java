package com.github.karixdev.polslcoursescheduleapi.course;

import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.CourseCell;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseMapper {
    private final CourseProperties properties;

    public Course mapCellToCourse(CourseCell courseCell, int startTimeHour, Schedule schedule) {
        LocalTime startsAt = getTime(courseCell.getTop(), startTimeHour, false);
        LocalTime endsAt = getTime(courseCell.getTop() + courseCell.getCh(), startTimeHour, true);
        DayOfWeek dayOfWeek = getDayOfWeek(courseCell.getLeft());
        Weeks weeks = getWeeks(courseCell.getLeft(), courseCell.getCw(), dayOfWeek);

        return Course.builder()
                .description(courseCell.getText())
                .startsAt(startsAt)
                .endsAt(endsAt)
                .dayOfWeek(dayOfWeek)
                .weeks(weeks)
                .schedule(schedule)
                .build();
    }

    private LocalTime getTime(int top, double startsAt, boolean addBorderToTop) {
        if (addBorderToTop) {
            top += properties.getCourseCellBorderSize();
        }

        int difference = top - properties.getFirstCellTopValue();
        double ratio = difference / properties.getOneHourBlockHeight();
        ratio /= 0.25;

        int totalNumOfQuarters = (int) Math.ceil(ratio);
        totalNumOfQuarters *= 15;

        double totalTime = totalNumOfQuarters / 60.0 + startsAt;
        int hours = (int) totalTime;
        int minutes = (int) ((totalTime - hours) * 60);

        return LocalTime.of(hours, minutes);
    }

    private DayOfWeek getDayOfWeek(int left) {
        Map<Integer, DayOfWeek> leftValueMap = properties.getDayOfWeekIntegerMap();

        return leftValueMap.keySet().stream()
                .filter(key -> left == key + properties.getWeekCellHalfOfWidth() || left == key)
                .findFirst()
                .map(leftValueMap::get)
                .orElseThrow();
    }

    private Weeks getWeeks(int left, int cw, DayOfWeek dayOfWeek) {
        if (cw == properties.getEveryWeekCwValue()) {
            return Weeks.EVERY;
        }

        boolean isEven = properties.getDayOfWeekIntegerMap()
                .entrySet()
                .stream()
                .anyMatch(entry -> entry.getKey().equals(left) && entry.getValue().equals(dayOfWeek));

        return isEven ? Weeks.EVEN : Weeks.ODD;
    }
}
