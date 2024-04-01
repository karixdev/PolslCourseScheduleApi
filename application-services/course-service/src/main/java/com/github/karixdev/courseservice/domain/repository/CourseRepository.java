package com.github.karixdev.courseservice.domain.repository;

import com.github.karixdev.courseservice.domain.entity.Course;

import java.util.Optional;
import java.util.UUID;

public interface CourseRepository {
    void save(Course course);

    Optional<Course> findById(UUID id);
}
