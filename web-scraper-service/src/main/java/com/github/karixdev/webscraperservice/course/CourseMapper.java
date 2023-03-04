package com.github.karixdev.webscraperservice.course;

import com.github.karixdev.webscraperservice.course.domain.Course;
import com.github.karixdev.webscraperservice.course.domain.CourseType;
import com.github.karixdev.webscraperservice.course.exception.NoScheduleStartTimeException;
import com.github.karixdev.webscraperservice.course.properties.CourseMapperProperties;
import com.github.karixdev.webscraperservice.planpolsl.domain.CourseCell;
import com.github.karixdev.webscraperservice.planpolsl.domain.PlanPolslResponse;
import com.github.karixdev.webscraperservice.planpolsl.domain.TimeCell;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseMapper {
    public Set<Course> map(PlanPolslResponse planPolslResponse) {
        LocalTime scheduleStartTime = getScheduleStartTime(planPolslResponse.timeCells());

        return planPolslResponse.courseCells().stream()
                .map(cell -> mapCellToCourse(cell, scheduleStartTime))
                .collect(Collectors.toSet());
    }

    private LocalTime getScheduleStartTime(Set<TimeCell> timeCells) {
        return timeCells.stream()
                .map(timeCell -> LocalTime.parse(timeCell.text().split("-")[0]))
                .min(LocalTime::compareTo)
                .orElseThrow(() -> {
                    throw new NoScheduleStartTimeException();
                });
    }

    private Course mapCellToCourse(CourseCell courseCell, LocalTime scheduleStartTime) {
        int scheduleStartTimeHour = scheduleStartTime.getHour();

        LocalTime startsAt = getTime(
                courseCell.top(),
                scheduleStartTimeHour,
                false
        );
        LocalTime endsAt = getTime(
                courseCell.top() + courseCell.ch(),
                scheduleStartTimeHour,
                true
        );

        CourseType courseType = getCourseType(courseCell.text());

        return new Course(
                startsAt,
                endsAt,
                courseType
        );
    }

    private LocalTime getTime(int top, int startsAt, boolean addBorderToTop) {
        if (addBorderToTop) {
            top += CourseMapperProperties.COURSE_CELL_BORDER_SIZE;
        }

        int difference = top - CourseMapperProperties.FIRST_CELL_TOP_VALUE;
        double ratio = difference / CourseMapperProperties.ONE_HOUR_CELL_HEIGHT;
        ratio /= 0.25;

        int totalNumOfQuarters = (int) Math.ceil(ratio);
        totalNumOfQuarters *= 15;

        double totalTime = totalNumOfQuarters / 60.0 + startsAt;
        int hours = (int) totalTime;
        int minutes = (int) ((totalTime - hours) * 60);

        return LocalTime.of(hours, minutes);
    }

    private CourseType getCourseType(String text) {
        String[] linesSplit = text.split("\n");
        String[] firstLineSplit = linesSplit[0].split(",");

        if (firstLineSplit.length == 1) {
            return CourseType.INFO;
        }

        return switch (firstLineSplit[1].trim()) {
            case "ćw" -> CourseType.PRACTICAL;
            case "lab" -> CourseType.LAB;
            case "proj" -> CourseType.PROJECT;
            case "wyk" -> CourseType.LECTURE;
            default -> CourseType.INFO;
        };
    }
}
