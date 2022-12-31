package com.github.karixdev.polslcoursescheduleapi.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("""
            SELECT schedule
            FROM Schedule schedule
            WHERE schedule.name = :name
            """)
    Optional<Schedule> findByName(@Param("name") String name);
}
