package com.github.karixdev.scheduleservice.repository;

import com.github.karixdev.scheduleservice.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    @Query("""
            SELECT schedule
            FROM Schedule schedule
            WHERE schedule.name = :name
            """)
    Optional<Schedule> findByName(@Param("name") String name);

    @Query("""
            SELECT schedule
            FROM Schedule schedule
            ORDER BY schedule.semester, schedule.groupNumber ASC
            """)
    List<Schedule> findAllOrderBySemesterAndGroupNumberAsc();

}
