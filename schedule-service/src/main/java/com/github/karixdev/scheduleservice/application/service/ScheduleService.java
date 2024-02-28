package com.github.karixdev.scheduleservice.application.service;

import com.github.karixdev.commonservice.dto.schedule.ScheduleRequest;
import com.github.karixdev.commonservice.dto.schedule.ScheduleResponse;
import com.github.karixdev.commonservice.exception.ResourceNotFoundException;
import com.github.karixdev.commonservice.exception.ValidationException;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.application.mapper.ScheduleMapper;
import com.github.karixdev.scheduleservice.application.event.producer.ScheduleEventProducer;
import com.github.karixdev.scheduleservice.infrastructure.dal.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository repository;
    private final ScheduleEventProducer producer;
    private final ScheduleMapper mapper;

    @Transactional
    public ScheduleResponse create(ScheduleRequest scheduleRequest) {
        if (repository.findByName(scheduleRequest.name()).isPresent()) {
            throw new ValidationException(
                    "name",
                    "name %s is unavailable".formatted(scheduleRequest.name())
            );
        }

        Schedule schedule = mapper.mapToEntity(scheduleRequest);
        repository.save(schedule);

        producer.produceScheduleCreateEvent(schedule);

        return mapper.mapToResponse(schedule);
    }

    public List<ScheduleResponse> findAll(Set<UUID> ids) {
        List<Schedule> schedules = repository.findAllOrderBySemesterAndGroupNumberAsc();
        log.info("{}", schedules);

        if (ids != null && !ids.isEmpty()) {
            schedules = schedules.stream()
                    .filter(schedule -> ids.contains(schedule.getId()))
                    .toList();
        }

        return schedules.stream().map(mapper::mapToResponse).toList();
    }

    public ScheduleResponse findById(UUID id) {
        Schedule schedule = findByIdOrElseThrow(id);
        return mapper.mapToResponse(schedule);
    }

    private Schedule findByIdOrElseThrow(UUID id) {
        String exceptionMsg = String.format("Schedule with id %s not found", id);
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(exceptionMsg));
    }

    @Transactional
    public ScheduleResponse update(UUID id, ScheduleRequest scheduleRequest) {
        Schedule schedule = findByIdOrElseThrow(id);

        Optional<Schedule> scheduleWithName = repository.findByName(scheduleRequest.name());

        if (scheduleWithName.isPresent() && !scheduleWithName.get().equals(schedule)) {
            throw new ValidationException(
                    "name",
                    "name %s is unavailable".formatted(scheduleRequest.name())
            );
        }

        schedule.setName(scheduleRequest.name());
        schedule.setType(scheduleRequest.type());
        schedule.setPlanPolslId(scheduleRequest.planPolslId());
        schedule.setSemester(scheduleRequest.semester());
        schedule.setGroupNumber(scheduleRequest.groupNumber());
        schedule.setWd(scheduleRequest.wd());

        producer.produceScheduleUpdateEvent(schedule);

        return mapper.mapToResponse(schedule);
    }

    @Transactional
    public void delete(UUID id) {
        Schedule schedule = findByIdOrElseThrow(id);

        repository.delete(schedule);
        producer.produceScheduleDeleteEvent(schedule);
    }

    public void requestScheduleCoursesUpdate(UUID id) {
        Schedule schedule = findByIdOrElseThrow(id);
        producer.produceScheduleUpdateEvent(schedule);
    }

    public void requestScheduleCoursesUpdateForAll() {
        repository.findAll().forEach(producer::produceScheduleUpdateEvent);
    }

}
