package com.github.karixdev.courseservice.infrastructure.dal.repository;

import com.github.karixdev.courseservice.domain.entity.Course;
import com.github.karixdev.courseservice.domain.repository.CourseRepository;
import com.github.karixdev.courseservice.infrastructure.dal.mapper.CourseJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CourseRepositoryJpaAdapter implements CourseRepository {

    private final CourseEntityRepository entityRepository;
    private final CourseJpaMapper mapper;

    @Override
    public void save(Course course) {
        entityRepository.save(mapper.toJpa(course));
    }

    @Override
    public void saveAll(Collection<Course> courses) {
        entityRepository.saveAll(
                courses.stream()
                        .map(mapper::toJpa)
                        .toList()
        );
    }

    @Override
    public void delete(Course course) {
        entityRepository.delete(mapper.toJpa(course));
    }

    @Override
    public void deleteAll(Collection<Course> courses) {
        entityRepository.deleteAll(
                courses.stream()
                        .map(mapper::toJpa)
                        .toList()
        );
    }

    @Override
    public void deleteByScheduleId(UUID scheduleId) {
        entityRepository.deleteByScheduleId(scheduleId);
    }

    @Override
    public Optional<Course> findById(UUID id) {
        return entityRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Course> findByScheduleId(UUID scheduleId) {
        return entityRepository.findByScheduleId(scheduleId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
