package com.github.karixdev.courseservice.matcher;

import com.github.karixdev.courseservice.entity.Course;
import lombok.RequiredArgsConstructor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.util.Collection;

@RequiredArgsConstructor
public class CourseDeepCollectionArgumentMatcher<T extends Collection<Course>> implements ArgumentMatcher<T> {

    private final T collection;

    public static <E extends Collection<Course>> E deepCourseCollectionEq(E collection) {
        return Mockito.argThat(new CourseDeepCollectionArgumentMatcher<>(collection));
    }

    @Override
    public boolean matches(T courses) {
        return collection.stream().allMatch((course1) -> {
            DeepCourseArgumentMatcher singleMatcher = new DeepCourseArgumentMatcher(course1);
            return courses.stream().anyMatch(singleMatcher::matches);
        });
    }

}
