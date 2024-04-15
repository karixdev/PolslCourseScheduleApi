package com.github.karixdev.courseservice.domain.repository;

import com.github.karixdev.courseservice.domain.entity.Course;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository {
    void save(Course course);
    void saveAll(Collection<Course> courses);

    void delete(Course course);
    void deleteAll(Collection<Course> courses);
    void deleteByScheduleId(UUID scheduleId);

    Optional<Course> findById(UUID id);

    List<Course> findByScheduleId(UUID scheduleId);
}
