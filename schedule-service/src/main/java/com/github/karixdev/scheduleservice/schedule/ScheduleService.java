package com.github.karixdev.scheduleservice.schedule;

import com.github.karixdev.scheduleservice.schedule.dto.ScheduleRequest;
import com.github.karixdev.scheduleservice.schedule.dto.ScheduleResponse;
import com.github.karixdev.scheduleservice.schedule.exception.ScheduleNameUnavailableException;
import com.github.karixdev.scheduleservice.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository repository;

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

        return new ScheduleResponse(
                schedule.getId(),
                schedule.getSemester(),
                schedule.getName(),
                schedule.getGroupNumber()
        );
    }

    public List<ScheduleResponse> findAll() {
        return repository.findAllOrderBySemesterAndGroupNumberAsc()
                .stream()
                .map(schedule -> new ScheduleResponse(
                        schedule.getId(),
                        schedule.getSemester(),
                        schedule.getName(),
                        schedule.getGroupNumber()
                ))
                .toList();
    }

    public ScheduleResponse findById(UUID id) {
        Schedule schedule = findByIdOrElseThrow(id);

        return new ScheduleResponse(
                schedule.getId(),
                schedule.getSemester(),
                schedule.getName(),
                schedule.getGroupNumber()
        );
    }

    private Schedule findByIdOrElseThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    throw new ResourceNotFoundException(
                            String.format(
                                    "Schedule with id %s not found",
                                    id)
                    );
                });
    }

    @Transactional
    public ScheduleResponse update(UUID id, ScheduleRequest scheduleRequest) {
        Schedule schedule = findByIdOrElseThrow(id);

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

        return new ScheduleResponse(
                schedule.getId(),
                schedule.getSemester(),
                schedule.getName(),
                schedule.getGroupNumber()
        );
    }

    public void delete(UUID id) {
        Schedule schedule = findByIdOrElseThrow(id);

        repository.delete(schedule);
    }
}
