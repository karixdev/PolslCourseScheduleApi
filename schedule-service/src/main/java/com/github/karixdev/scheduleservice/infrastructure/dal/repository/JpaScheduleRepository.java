package com.github.karixdev.scheduleservice.infrastructure.dal.repository;

import com.github.karixdev.scheduleservice.infrastructure.dal.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaScheduleRepository extends JpaRepository<ScheduleEntity, UUID> {

    @Query("""
            SELECT schedule
            FROM Schedule schedule
            WHERE schedule.planPolslId = :planPolslId
            """)
    Optional<ScheduleEntity> findByPlanPolslId(@Param("planPolslId") Integer planPolslId);

}
