package com.github.karixdev.polslcoursescheduleapi.course;

import com.github.karixdev.polslcoursescheduleapi.course.exception.EmptyCourseCellListException;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.PlanPolslResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleService;
import com.github.karixdev.polslcoursescheduleapi.schedule.exception.ScheduleNoStartTimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository repository;
    private final CourseMapper mapper;

    @Transactional
    public void updateScheduleCourses(PlanPolslResponse response, Schedule schedule) {
        List<LocalTime> times = response.getTimeCells().stream()
                .map(timeCell -> LocalTime.parse(timeCell.getText().split("-")[0]))
                .sorted()
                .toList();

        if (times.isEmpty()) {
            throw new ScheduleNoStartTimeException();
        }

        int startTimeHour = times.get(0).getHour();

        if (response.getCourseCells().isEmpty()) {
            throw new EmptyCourseCellListException();
        }

        List<Course> courses = response.getCourseCells().stream()
                .map(courseCell -> mapper.mapCellToCourse(
                        courseCell,
                        startTimeHour,
                        schedule))
                .toList();

        repository.deleteAll(schedule.getCourses());
        repository.saveAll(courses);
    }
}
