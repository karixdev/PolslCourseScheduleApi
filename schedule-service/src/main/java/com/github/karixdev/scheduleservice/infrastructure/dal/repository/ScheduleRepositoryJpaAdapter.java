package com.github.karixdev.scheduleservice.infrastructure.dal.repository;

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
    public Optional<Schedule> findByName(String name) {
        return jpaRepository.findByName(name).map(entityMapper::toDomainEntity);
    }

    @Override
    public Optional<Schedule> findById(UUID id) {
        return jpaRepository.findById(id).map(entityMapper::toDomainEntity);
    }

    @Override
    public List<Schedule> findAllOrderBySemesterAndGroupNumberAsc() {
        return jpaRepository.findAllOrderBySemesterAndGroupNumberAsc()
                .stream()
                .map(entityMapper::toDomainEntity)
                .toList();
    }

    @Override
    public List<Schedule> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(entityMapper::toDomainEntity)
                .toList();
    }

}
