package com.github.karixdev.polslcoursescheduleapi.schedule;

import com.github.karixdev.polslcoursescheduleapi.course.CourseService;
import com.github.karixdev.polslcoursescheduleapi.course.exception.EmptyCourseCellListException;
import com.github.karixdev.polslcoursescheduleapi.course.payload.response.CourseResponse;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.PlanPolslService;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.exception.PlanPolslEmptyResponseException;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.exception.PlanPolslWebClientException;
import com.github.karixdev.polslcoursescheduleapi.planpolsl.payload.PlanPolslResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.exception.ScheduleNameNotAvailableException;
import com.github.karixdev.polslcoursescheduleapi.schedule.exception.ScheduleNoStartTimeException;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.request.ScheduleRequest;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleCollectionResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleWithCoursesResponse;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleResponse;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.shared.exception.ResourceNotFoundException;
import com.github.karixdev.polslcoursescheduleapi.shared.payload.response.SuccessResponse;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.DayOfWeek;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
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
        Schedule schedule = getById(id);

        repository.delete(schedule);

        return new SuccessResponse();
    }


    public void updateSchedules() {
        repository.findAllSchedules().forEach(this::updateSchedule);
    }

    @Transactional(dontRollbackOn = {
            EmptyCourseCellListException.class,
            PlanPolslWebClientException.class,
            PlanPolslEmptyResponseException.class,
            ScheduleNoStartTimeException.class
    })
    private void updateSchedule(Schedule schedule) {
        try {
            PlanPolslResponse response =
                    planPolslService.getPlanPolslResponse(schedule);

            courseService.updateScheduleCourses(response, schedule);
        } catch (EmptyCourseCellListException |
                 PlanPolslWebClientException |
                 PlanPolslEmptyResponseException |
                 ScheduleNoStartTimeException e
        ) {
            log.error("error while updating schedule with id: %d".formatted(schedule.getId()), e);
        }
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

    public ScheduleWithCoursesResponse getSchedulesWithCourses(Long id) {
        Schedule schedule = getById(id);

        Map<DayOfWeek, List<CourseResponse>> courses = new LinkedHashMap<>();

        schedule.getCourses().stream()
                .sorted((course1, course2) -> {
                    if (course1.getDayOfWeek().compareTo(course2.getDayOfWeek()) != 0) {
                        return course1.getDayOfWeek().compareTo(course2.getDayOfWeek());
                    }

                    return course1.getStartsAt().compareTo(course2.getStartsAt());
                })
                .forEach(course -> {
                    if (!courses.containsKey(course.getDayOfWeek())) {
                        courses.put(course.getDayOfWeek(), new ArrayList<>());
                    }

                    courses.get(course.getDayOfWeek()).add(new CourseResponse(course));
                });

        return new ScheduleWithCoursesResponse(schedule, courses);
    }

    public Schedule getById(Long id) {
        return repository.findScheduleById(id)
                .orElseThrow(() -> {
                    throw new ResourceNotFoundException("Schedule with provided id not found");
                });
    }

    public void manualUpdate(Long id) {
        Schedule schedule = getById(id);

        asyncManualUpdate(schedule);
    }

    @Async
    private void asyncManualUpdate(Schedule schedule) {
        updateSchedule(schedule);
    }
}
