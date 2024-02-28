package com.github.karixdev.scheduleservice.domain.repository;

import com.github.karixdev.scheduleservice.domain.entity.Schedule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleRepository {

    void save(Schedule schedule);
    void delete(Schedule schedule);

    Optional<Schedule> findByName(String name);
    Optional<Schedule> findById(UUID id);

    List<Schedule> findAllOrderBySemesterAndGroupNumberAsc();
    List<Schedule> findAll();

}
