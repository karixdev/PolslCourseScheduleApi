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

        List<Course> courses = response.getCourseCells().stream()
                .map(courseCell -> mapper.mapCellToCourse(
                        courseCell,
                        startTimeHour,
                        schedule))
                .toList();

        Set<Course> currentCourses = schedule.getCourses();

        Set<Course> coursesToDelete = new HashSet<>();
        Set<Course> coursesToSave = new HashSet<>();

        // Delete changed courses and save new
        for (Course course : courses) {
            boolean isNew = true;

            for (Course currentCourse : currentCourses) {
                if (course.getDescription().equals(currentCourse.getDescription()) &&
                        !doCoursesHaveSameParameters(course, currentCourse) &&
                        courses.stream().noneMatch(aCourse ->
                                doCoursesHaveSameParameters(aCourse, currentCourse))
                ) {
                    coursesToDelete.add(currentCourse);
                    break;
                }

                if (doCoursesHaveSameParameters(course, currentCourse)) {
                    isNew = false;
                }
            }

            if (isNew) {
                coursesToSave.add(course);
            }
        }

        // Delete old courses
        for (Course currentCourse : currentCourses) {
            boolean shouldBeDeleted = true;
            for (Course course : courses) {
                if (doCoursesHaveSameParameters(currentCourse, course)) {
                    shouldBeDeleted = false;
                    break;
                }
            }

            if (shouldBeDeleted) {
                coursesToDelete.add(currentCourse);
            }
        }

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
