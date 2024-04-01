package com.github.karixdev.courseservice.domain.repository;

import com.github.karixdev.courseservice.domain.entity.Course;

public interface CourseRepository {
    void save(Course course);
}
