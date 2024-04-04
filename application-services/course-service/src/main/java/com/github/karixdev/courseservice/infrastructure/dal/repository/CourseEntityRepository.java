package com.github.karixdev.courseservice.infrastructure.dal.repository;

import com.github.karixdev.courseservice.infrastructure.dal.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseEntityRepository extends JpaRepository<CourseEntity, UUID> {

    @Modifying
    @Query("""
            DELETE CourseEntity course
            WHERE course.scheduleId = :scheduleId
            """)
    void deleteByScheduleId(@Param("scheduleId") UUID scheduleId);

    @Query("""
            SELECT course
            FROM CourseEntity course
            WHERE course.scheduleId = :scheduleId
            """)
    List<CourseEntity> findByScheduleId(@Param("scheduleId") UUID scheduleId);

}
