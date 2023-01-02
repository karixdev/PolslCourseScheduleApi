package com.github.karixdev.polslcoursescheduleapi.schedule;

import com.github.karixdev.polslcoursescheduleapi.course.CourseService;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.PlanPolslService;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.PlanPolslResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.exception.ScheduleNameNotAvailableException;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.request.ScheduleRequest;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleCollectionResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleResponse;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.shared.exception.ResourceNotFoundException;
import com.github.karixdev.polslcoursescheduleapi.shared.payload.response.SuccessResponse;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository repository;
    private final PlanPolslService planPolslService;
    private final CourseService courseService;

    @Transactional
    public ScheduleResponse add(ScheduleRequest payload, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();

        String name = payload.getName();

        if (repository.findByName(name).isPresent()) {
            throw new ScheduleNameNotAvailableException();
        }

        Schedule schedule = Schedule.builder()
                .type(payload.getType())
                .planPolslId(payload.getPlanPolslId())
                .semester(payload.getSemester())
                .name(name)
                .groupNumber(payload.getGroupNumber())
                .addedBy(user)
                .build();

        schedule = repository.save(schedule);

        return new ScheduleResponse(schedule);
    }

    @Transactional
    public SuccessResponse delete(Long id) {
        Schedule schedule = repository.findById(id)
                .orElseThrow(() -> {
                    throw new ResourceNotFoundException("Schedule with provided id not found");
                });

        repository.delete(schedule);

        return new SuccessResponse();
    }

    @Transactional
    public void updateSchedules() {
        repository.findAll().forEach(schedule -> {
            PlanPolslResponse response =
                    planPolslService.getPlanPolslResponse(schedule);

            courseService.updateScheduleCourses(response, schedule);
        });
    }

    public ScheduleCollectionResponse getAll() {
        Map<Integer, List<ScheduleResponse>> semesters =
                new HashMap<>();

        repository.findAllOrderByGroupNumberAndSemesterAsc().forEach(schedule -> {
            Integer semester = schedule.getSemester();

            if (!semesters.containsKey(semester)) {
                semesters.put(semester, new ArrayList<>());
            }
            semesters.get(semester).add(new ScheduleResponse(schedule));
        });

        return new ScheduleCollectionResponse(semesters);
    }
}
