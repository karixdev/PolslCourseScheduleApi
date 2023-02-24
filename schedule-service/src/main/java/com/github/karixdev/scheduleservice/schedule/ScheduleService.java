package com.github.karixdev.scheduleservice.schedule;

import com.github.karixdev.scheduleservice.schedule.dto.ScheduleRequest;
import com.github.karixdev.scheduleservice.schedule.dto.ScheduleResponse;
import com.github.karixdev.scheduleservice.schedule.exception.ScheduleNameUnavailableException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
