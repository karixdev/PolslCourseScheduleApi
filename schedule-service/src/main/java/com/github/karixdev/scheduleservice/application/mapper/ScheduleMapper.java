package com.github.karixdev.scheduleservice.application.mapper;

import com.github.karixdev.commonservice.dto.schedule.ScheduleRequest;
import com.github.karixdev.commonservice.dto.schedule.ScheduleResponse;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import org.springframework.stereotype.Component;

@Component
public class ScheduleMapper {

    public Schedule mapToEntity(ScheduleRequest request) {
        return Schedule.builder()
                .type(request.type())
                .planPolslId(request.planPolslId())
                .semester(request.semester())
                .name(request.name())
                .groupNumber(request.groupNumber())
                .wd(request.wd())
                .build();
    }

    public ScheduleResponse mapToResponse(Schedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .semester(schedule.getSemester())
                .name(schedule.getName())
                .groupNumber(schedule.getGroupNumber())
                .build();
    }

}
