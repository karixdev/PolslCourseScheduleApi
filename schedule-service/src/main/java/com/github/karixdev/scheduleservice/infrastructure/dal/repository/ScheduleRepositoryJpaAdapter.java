package com.github.karixdev.scheduleservice.infrastructure.dal.repository;

import com.github.karixdev.scheduleservice.application.filter.ScheduleFilter;
import com.github.karixdev.scheduleservice.application.pagination.Page;
import com.github.karixdev.scheduleservice.application.pagination.PageInfo;
import com.github.karixdev.scheduleservice.application.pagination.PageRequest;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import com.github.karixdev.scheduleservice.infrastructure.dal.mapper.ScheduleJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ScheduleRepositoryJpaAdapter implements ScheduleRepository {

    private final JpaScheduleRepository jpaRepository;
    private final ScheduleJpaMapper entityMapper;

    @Override
    public void save(Schedule schedule) {
        jpaRepository.save(entityMapper.toJpaEntity(schedule));
    }

    @Override
    public void delete(Schedule schedule) {
        jpaRepository.delete(entityMapper.toJpaEntity(schedule));
    }

    @Override
    public Optional<Schedule> findById(UUID id) {
        return jpaRepository.findById(id).map(entityMapper::toDomainEntity);
    }

    @Override
    public Optional<Schedule> findByPlanPolslId(Integer planPolslId) {
        return jpaRepository.findByPlanPolslId(planPolslId).map(entityMapper::toDomainEntity);
    }

    @Override
    public List<Schedule> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(entityMapper::toDomainEntity)
                .toList();
    }

    @Override
    public Page<Schedule> findByFilterAndPaginate(ScheduleFilter filter, PageRequest pageRequest) {
        var entitiesPage = jpaRepository.findByFilterAndPaginate(filter, pageRequest);

        PageInfo pageInfo = PageInfo.builder()
                .page(entitiesPage.getPageable().getPageNumber())
                .size(entitiesPage.getPageable().getPageSize())
                .numberOfElements(entitiesPage.getNumberOfElements())
                .totalPages(entitiesPage.getTotalPages())
                .totalElements(entitiesPage.getTotalElements())
                .isLast(entitiesPage.isLast())
                .build();

        List<Schedule> schedules = entitiesPage.getContent()
                .stream()
                .map(entityMapper::toDomainEntity)
                .toList();

        return Page.<Schedule>builder()
                .content(schedules)
                .pageInfo(pageInfo)
                .build();
    }

    @Override
    public List<Schedule> findByMajorAndSemester(String major, Integer group) {
        return jpaRepository.findByMajorAndSemester(major, group)
                .stream()
                .map(entityMapper::toDomainEntity)
                .toList();
    }

    @Override
    public List<String> findUniqueMajorsOrderedAlphabetically() {
        return jpaRepository.findUniqueMajorsOrderedAlphabetically();
    }

    @Override
    public List<Integer> findSemestersByMajorOrderAsc(String major) {
        return jpaRepository.findSemestersByMajorOrderAsc(major);
    }

}
