package com.github.karixdev.courseservice.matcher;

import com.github.karixdev.courseservice.entity.Course;
import lombok.RequiredArgsConstructor;
import org.mockito.ArgumentMatcher;

import java.util.Objects;

@RequiredArgsConstructor
public class DeepCourseArgumentMatcher implements ArgumentMatcher<Course> {

    private final Course course;

    public static DeepCourseArgumentMatcher deepCourseMatcher(Course course) {
        return new DeepCourseArgumentMatcher(course);
    }

    @Override
    public boolean matches(Course o) {
        return Objects.equals(o. getId(), course.getId()) &&
                Objects.equals(o.getScheduleId(), course.getScheduleId()) &&
                Objects.equals(o.getName(), course.getName()) &&
                o.getCourseType() == course.getCourseType() &&
                Objects.equals(o.getTeachers(), course.getTeachers()) &&
                Objects.equals(o.getClassroom(), course.getClassroom()) &&
                Objects.equals(o.getAdditionalInfo(), course.getAdditionalInfo()) &&
                o.getDayOfWeek() == course.getDayOfWeek() &&
                o.getWeekType() == course.getWeekType() &&
                Objects.equals(o.getStartsAt(), course.getStartsAt()) &&
                Objects.equals(o.getEndsAt(), course.getEndsAt());
    }

}
