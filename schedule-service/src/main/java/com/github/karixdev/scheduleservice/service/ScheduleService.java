package com.github.karixdev.scheduleservice.service;

import com.github.karixdev.scheduleservice.dto.ScheduleRequest;
import com.github.karixdev.scheduleservice.dto.ScheduleResponse;
import com.github.karixdev.scheduleservice.entity.Schedule;
import com.github.karixdev.scheduleservice.exception.ResourceNotFoundException;
import com.github.karixdev.scheduleservice.exception.ScheduleNameUnavailableException;
import com.github.karixdev.scheduleservice.message.ScheduleEventType;
import com.github.karixdev.scheduleservice.producer.ScheduleEventProducer;
import com.github.karixdev.scheduleservice.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository repository;
    private final ScheduleEventProducer producer;

    @Transactional
    public ScheduleResponse create(ScheduleRequest scheduleRequest) {
        if (repository.findByName(scheduleRequest.name()).isPresent()) {
            throw new ScheduleNameUnavailableException(
                    scheduleRequest.name());
        }

        Schedule schedule = repository.save(Schedule.builder()
                .type(scheduleRequest.type())
                .planPolslId(scheduleRequest.planPolslId())
                .semester(scheduleRequest.semester())
                .name(scheduleRequest.name())
                .groupNumber(scheduleRequest.groupNumber())
                .wd(scheduleRequest.wd())
                .build());

        producer.produceScheduleEventMessage(
                schedule.getId(),
                ScheduleEventType.CREATE
        );

        return new ScheduleResponse(
                schedule.getId(),
                schedule.getSemester(),
                schedule.getName(),
                schedule.getGroupNumber()
        );
    }

    public List<ScheduleResponse> findAll(Set<UUID> ids) {
        List<Schedule> schedules = repository.findAllOrderBySemesterAndGroupNumberAsc();

        if (ids != null && ids.size() > 0) {
            schedules = schedules.stream()
                    .filter(schedule -> ids.contains(schedule.getId()))
                    .toList();
        }

        return schedules.stream()
                .map(schedule -> new ScheduleResponse(
                        schedule.getId(),
                        schedule.getSemester(),
                        schedule.getName(),
                        schedule.getGroupNumber()
                ))
                .toList();
    }

    public ScheduleResponse findById(UUID id) {
        Schedule schedule = findByIdOrElseThrow(id, false);

        return new ScheduleResponse(
                schedule.getId(),
                schedule.getSemester(),
                schedule.getName(),
                schedule.getGroupNumber()
        );
    }

    public Schedule findByIdOrElseThrow(UUID id, boolean eagerLoad) {
        Optional<Schedule> optionalSchedule = repository.findById(id);

        return optionalSchedule.orElseThrow(() -> {
            throw new ResourceNotFoundException(
                    String.format(
                            "Schedule with id %s not found",
                            id)
            );
        });
    }

    @Transactional
    public ScheduleResponse update(UUID id, ScheduleRequest scheduleRequest) {
        Schedule schedule = findByIdOrElseThrow(id, false);

        Optional<Schedule> scheduleWithName = repository.findByName(scheduleRequest.name());

        if (scheduleWithName.isPresent() && !scheduleWithName.get().equals(schedule)) {
            throw new ScheduleNameUnavailableException(
                    scheduleRequest.name());
        }

        schedule.setName(scheduleRequest.name());
        schedule.setType(scheduleRequest.type());
        schedule.setPlanPolslId(scheduleRequest.planPolslId());
        schedule.setSemester(scheduleRequest.semester());
        schedule.setGroupNumber(scheduleRequest.groupNumber());
        schedule.setWd(scheduleRequest.wd());

        repository.save(schedule);

        producer.produceScheduleEventMessage(
                schedule.getId(),
                ScheduleEventType.UPDATE
        );

        return new ScheduleResponse(
                schedule.getId(),
                schedule.getSemester(),
                schedule.getName(),
                schedule.getGroupNumber()
        );
    }

    @Transactional
    public void delete(UUID id) {
        Schedule schedule = findByIdOrElseThrow(id, false);

        repository.delete(schedule);

        producer.produceScheduleEventMessage(
                schedule.getId(),
                ScheduleEventType.DELETE
        );
    }

    public void requestScheduleCoursesUpdate(UUID id) {
        Schedule schedule = findByIdOrElseThrow(id, false);

        producer.produceScheduleEventMessage(
                schedule.getId(),
                ScheduleEventType.UPDATE
        );

    }

    public void requestScheduleCoursesUpdateForAll() {
        repository.findAll().forEach(schedule ->
                producer.produceScheduleEventMessage(
                        schedule.getId(),
                        ScheduleEventType.UPDATE
                ));
    }
}
