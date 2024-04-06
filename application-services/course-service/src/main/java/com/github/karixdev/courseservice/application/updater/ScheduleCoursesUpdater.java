package com.github.karixdev.courseservice.application.updater;

import com.github.karixdev.courseservice.application.dal.TransactionManager;
import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScheduleCoursesUpdater {

    private final CourseRepository repository;
    private final TransactionManager transactionManager;

    public void update(Set<Course> current, Set<Course> received) {
        Set<Course> coursesToSave = received.stream()
                .filter(retrievedCourse -> current.stream()
                        .noneMatch(currentCourse -> areNonIdParamsEqualPredicate().test(retrievedCourse, currentCourse)))
                .collect(Collectors.toSet());

        Set<Course> coursesToDelete = current.stream()
                .filter(currentCourse -> received.stream()
                        .noneMatch(retrievedCourse ->
                                areNonIdParamsEqualPredicate().test(retrievedCourse, currentCourse)))
                .collect(Collectors.toSet());

        transactionManager.execute(() -> {
            repository.saveAll(coursesToSave);
            repository.deleteAll(coursesToDelete);
        });
    }

    private BiPredicate<Course, Course> areNonIdParamsEqualPredicate() {
        return (course1, course2) -> Objects.equals(course1.getName(), course2.getName()) &&
                course1.getCourseType() == course2.getCourseType() &&
                Objects.equals(course1.getTeachers(), course2.getTeachers()) &&
                Objects.equals(course1.getClassrooms(), course2.getClassrooms()) &&
                Objects.equals(course1.getAdditionalInfo(), course2.getAdditionalInfo()) &&
                course1.getDayOfWeek() == course2.getDayOfWeek() &&
                course1.getWeekType() == course2.getWeekType() &&
                Objects.equals(course1.getStartsAt(), course2.getStartsAt()) &&
                Objects.equals(course1.getEndsAt(), course2.getEndsAt());
    }

}
