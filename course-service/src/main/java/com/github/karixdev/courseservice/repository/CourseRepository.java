package com.github.karixdev.courseservice.repository;

import com.github.karixdev.courseservice.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {}