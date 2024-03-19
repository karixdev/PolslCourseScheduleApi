package com.github.karixdev.scheduleservice.infrastructure.rest.mapper;

import com.github.karixdev.scheduleservice.application.dto.PlanPolslDataDTO;
import com.github.karixdev.scheduleservice.application.dto.ScheduleDTO;
import com.github.karixdev.scheduleservice.application.mapper.ModelMapper;
import com.github.karixdev.scheduleservice.infrastructure.rest.payload.response.PlanPolslDataResponse;
import com.github.karixdev.scheduleservice.infrastructure.rest.payload.response.ScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleResponseMapper implements ModelMapper<ScheduleDTO, ScheduleResponse> {

    private final ModelMapper<PlanPolslDataDTO, PlanPolslDataResponse> planPolslMapper;

    @Override
    public ScheduleResponse map(ScheduleDTO input) {
        return ScheduleResponse.builder()
                .id(input.id())
                .semester(input.semester())
                .major(input.major())
                .group(input.groupNumber())
                .planPoslData(planPolslMapper.map(input.planPolslData()))
                .build();
    }

}
