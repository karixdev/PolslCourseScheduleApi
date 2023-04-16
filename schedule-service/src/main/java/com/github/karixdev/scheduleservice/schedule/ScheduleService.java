package com.github.karixdev.scheduleservice.schedule;

import com.github.karixdev.scheduleservice.course.CourseComparator;
import com.github.karixdev.scheduleservice.course.CourseMapper;
import com.github.karixdev.scheduleservice.course.dto.CourseResponse;
import com.github.karixdev.scheduleservice.schedule.dto.ScheduleRequest;
import com.github.karixdev.scheduleservice.schedule.dto.ScheduleResponse;
import com.github.karixdev.scheduleservice.schedule.exception.ScheduleNameUnavailableException;
import com.github.karixdev.scheduleservice.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository repository;
    private final ScheduleProducer producer;
    private final CourseComparator courseComparator;
    private final CourseMapper courseMapper;

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

        producer.sendScheduleUpdateRequest(schedule);

        return new ScheduleResponse(
                schedule.getId(),
                schedule.getSemester(),
                schedule.getName(),
                schedule.getGroupNumber()
        );
    }

    public List<ScheduleResponse> findAll(UUID[] ids) {
        List<Schedule> schedules = repository.findAllOrderBySemesterAndGroupNumberAsc();

        if (ids.length > 0) {
            schedules = schedules.stream()
                    .filter(schedule -> Arrays.asList(ids).contains(schedule.getId()))
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
        Optional<Schedule> optionalSchedule;

        if (eagerLoad) {
            optionalSchedule = repository.findByIdWithCourses(id);
        } else {
            optionalSchedule = repository.findById(id);
        }

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

        producer.sendScheduleUpdateRequest(schedule);

        return new ScheduleResponse(
                schedule.getId(),
                schedule.getSemester(),
                schedule.getName(),
                schedule.getGroupNumber()
        );
    }

    public void delete(UUID id) {
        Schedule schedule = findByIdOrElseThrow(id, false);

        repository.delete(schedule);
    }

    public void requestScheduleCoursesUpdate(UUID id) {
        Schedule schedule = findByIdOrElseThrow(id, false);

        producer.sendScheduleUpdateRequest(schedule);
    }

    public void requestScheduleCoursesUpdateForAll() {
        repository.findAll()
                .forEach(producer::sendScheduleUpdateRequest);
    }

    public List<CourseResponse> findScheduleCourses(UUID id) {
        Schedule schedule = findByIdOrElseThrow(id, true);

        return schedule.getCourses().stream()
                .sorted(courseComparator)
                .map(courseMapper::map)
                .toList();
    }
}
