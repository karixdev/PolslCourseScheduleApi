package com.github.karixdev.scheduleservice.domain.repository;

import com.github.karixdev.scheduleservice.application.filter.ScheduleFilter;
import com.github.karixdev.scheduleservice.application.pagination.Page;
import com.github.karixdev.scheduleservice.application.pagination.PageRequest;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleRepository {

    void save(Schedule schedule);
    void delete(Schedule schedule);

    Optional<Schedule> findById(UUID id);
    Optional<Schedule> findByPlanPolslId(Integer planPolslId);

    List<Schedule> findAll();
    Page<Schedule> findByFilterAndPaginate(ScheduleFilter filter, PageRequest pageRequest);
    List<Schedule> findByMajorAndSemester(String major, Integer group);
    List<String> findUniqueMajorsOrderedAlphabetically();
    List<Integer> findSemestersByMajorOrderAsc(String major);
}
