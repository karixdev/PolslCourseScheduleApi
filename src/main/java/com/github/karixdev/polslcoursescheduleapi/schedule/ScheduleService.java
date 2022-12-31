package com.github.karixdev.polslcoursescheduleapi.schedule;

import com.github.karixdev.polslcoursescheduleapi.schedule.exception.ScheduleNameNotAvailableException;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.request.ScheduleRequest;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleResponse;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository repository;

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
}
