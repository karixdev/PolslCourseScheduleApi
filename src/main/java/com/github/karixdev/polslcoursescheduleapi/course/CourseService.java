package com.github.karixdev.polslcoursescheduleapi.course;

import com.github.karixdev.polslcoursescheduleapi.course.exception.EmptyCourseCellListException;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.PlanPolslResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleService;
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
    private final ScheduleService scheduleService;
    private final CourseMapper mapper;

    @Transactional
    public void updateScheduleCourses(PlanPolslResponse response, Schedule schedule) {
        LocalTime startTime = scheduleService
                .getScheduleStartTime(response.getTimeCells());

        int startTimeHour = startTime.getHour();

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
