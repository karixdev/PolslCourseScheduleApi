package com.github.karixdev.courseservice.repository;

import com.github.karixdev.courseservice.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    @Query("""
            SELECT c
            FROM CourseEntity c
            WHERE c.scheduleId = :scheduleId
            """)
    List<Course> findByScheduleId(@Param("scheduleId") UUID scheduleId);

}