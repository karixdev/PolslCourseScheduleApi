package com.github.karixdev.scheduleservice.course;

import com.github.karixdev.scheduleservice.course.dto.CourseRequest;
import com.github.karixdev.scheduleservice.course.dto.CourseResponse;
import com.github.karixdev.scheduleservice.schedule.Schedule;
import com.github.karixdev.scheduleservice.schedule.ScheduleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository repository;
    private final ScheduleService scheduleService;
    private final CourseMapper mapper;

    @Transactional
    public void updateScheduleCourses(Schedule schedule, Set<Course> retrievedCourses) {
        Set<Course> currentCourses = schedule.getCourses();

        Set<Course> coursesToSave = retrievedCourses.stream()
                .filter(retrievedCourse -> currentCourses.stream()
                        .noneMatch(currentCourse -> hasSameParameters(
                                retrievedCourse,
                                currentCourse)
                        ))
                .collect(Collectors.toSet());

        Set<Course> coursesToDelete = currentCourses.stream()
                .filter(currentCourse -> retrievedCourses.stream()
                        .noneMatch(retrievedCourse -> hasSameParameters(
                                retrievedCourse,
                                currentCourse)
                        ))
                .collect(Collectors.toSet());

        repository.deleteAll(coursesToDelete);
        repository.saveAll(coursesToSave);
    }

    private boolean hasSameParameters(Course course1, Course course2) {
        return course1.getName().equals(course2.getName()) &&
                course1.getCourseType().equals(course2.getCourseType()) &&
                course1.getTeachers().equals(course2.getTeachers()) &&
                course1.getClassroom().equals(course2.getClassroom()) &&
                course1.getAdditionalInfo().equals(course2.getAdditionalInfo()) &&
                course1.getWeekType().equals(course2.getWeekType()) &&
                course1.getDayOfWeek().equals(course2.getDayOfWeek()) &&
                course1.getEndsAt().equals(course2.getEndsAt()) &&
                course1.getStartsAt().equals(course2.getStartsAt());
    }

    @Transactional
    public CourseResponse create(CourseRequest courseRequest) {
        Schedule schedule = scheduleService.findByIdOrElseThrow(
                courseRequest.getScheduleId(), false);

        Course course = Course.builder()
                .startsAt(courseRequest.getStartsAt())
                .endsAt(courseRequest.getEndsAt())
                .name(courseRequest.getName())
                .courseType(courseRequest.getCourseType())
                .teachers(courseRequest.getTeachers())
                .dayOfWeek(courseRequest.getDayOfWeek())
                .weekType(courseRequest.getWeekType())
                .classroom(courseRequest.getClassrooms())
                .additionalInfo(courseRequest.getAdditionalInfo())
                .schedule(schedule)
                .build();

        repository.save(course);

        return mapper.map(course);
    }
}
