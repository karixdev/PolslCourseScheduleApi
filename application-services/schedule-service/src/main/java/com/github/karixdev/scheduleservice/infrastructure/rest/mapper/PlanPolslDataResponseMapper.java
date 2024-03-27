package com.github.karixdev.scheduleservice.infrastructure.rest.mapper;

import com.github.karixdev.scheduleservice.application.dto.PlanPolslDataDTO;
import com.github.karixdev.scheduleservice.application.mapper.ModelMapper;
import com.github.karixdev.scheduleservice.infrastructure.rest.payload.response.PlanPolslDataResponse;
import org.springframework.stereotype.Component;

@Component
public class PlanPolslDataResponseMapper implements ModelMapper<PlanPolslDataDTO, PlanPolslDataResponse> {

    @Override
    public PlanPolslDataResponse map(PlanPolslDataDTO input) {
        return PlanPolslDataResponse.builder()
                .id(input.id())
                .type(input.type())
                .weekDays(input.weekDays())
                .build();
    }

}
