package com.github.karixdev.polslcoursescheduleapi.course;

import com.github.karixdev.polslcoursescheduleapi.course.exception.EmptyCourseCellListException;
import com.github.karixdev.polslcoursescheduleapi.discord.DiscordApiService;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.PlanPolslResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.schedule.exception.ScheduleNoStartTimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {
    private final CourseRepository repository;
    private final CourseMapper mapper;

    private final DiscordApiService discordApiService;

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

        Set<Course> courses = response.getCourseCells().stream()
                .map(courseCell -> mapper.mapCellToCourse(
                        courseCell,
                        startTimeHour,
                        schedule))
                .collect(Collectors.toSet());

        Set<Course> currentCourses = schedule.getCourses();

        Set<Course> coursesToDelete = new HashSet<>();
        Set<Course> coursesToSave = new HashSet<>();

        selectChangedCoursesAndNewCourses(
                currentCourses,
                courses,
                coursesToSave,
                coursesToDelete
        );

        selectOutdatedCourses(
                currentCourses,
                courses,
                coursesToDelete
        );

        repository.deleteAll(coursesToDelete);
        repository.saveAll(coursesToSave);

        try {
            if (!coursesToSave.isEmpty() ||
                    !coursesToDelete.isEmpty()) {
                sendNotification(schedule);
            }
        } catch (RuntimeException e) {
            log.error("error while sending discord notification", e);
        }
    }

    private void selectOutdatedCourses(
            Set<Course> currentCourses,
            Set<Course> newCourses,
            Set<Course> selected
    ) {
        currentCourses.stream()
                .filter(currentCourse -> newCourses.stream()
                        .noneMatch(newCourse ->
                                doCoursesHaveSameParameters(
                                        newCourse,
                                        currentCourse)))
                .forEach(selected::add);
    }

    private void selectChangedCoursesAndNewCourses(
            Set<Course> currentCourses,
            Set<Course> newCourses,
            Set<Course> selectedNew,
            Set<Course> selectedChanged
    ) {
        // Changed
        for (Course newCourse : newCourses) {
            for (Course currentCourse : currentCourses) {
                if (hasCourseChanged(newCourse, currentCourse, newCourses)) {
                    selectedChanged.add(currentCourse);
                    break;
                }
            }
        }

        // New
        newCourses.stream()
                .filter(newCourse -> currentCourses.stream()
                        .noneMatch(currentCourse ->
                                doCoursesHaveSameParameters(
                                        newCourse,
                                        currentCourse)))
                .forEach(selectedNew::add);
    }

    private boolean hasCourseChanged(Course newCourse, Course currentCourse, Set<Course> newCourses) {
        return newCourse.getDescription().equals(currentCourse.getDescription()) &&
                !doCoursesHaveSameParameters(newCourse, currentCourse) &&
                newCourses.stream().noneMatch(aCourse ->
                        doCoursesHaveSameParameters(aCourse, currentCourse));
    }

    private boolean doCoursesHaveSameParameters(Course course1, Course course2) {
        return course1.getDescription().equals(course2.getDescription()) &&
                course1.getWeeks().equals(course2.getWeeks()) &&
                course1.getDayOfWeek().equals(course2.getDayOfWeek()) &&
                course1.getEndsAt().equals(course2.getEndsAt()) &&
                course1.getStartsAt().equals(course2.getStartsAt());
    }

    private void sendNotification(Schedule schedule) {
        discordApiService.sendScheduleCoursesUpdateMessage(schedule);
    }
}
