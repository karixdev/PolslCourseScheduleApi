package com.github.karixdev.scheduleservice.application.mapper;

import com.github.karixdev.scheduleservice.application.dto.PlanPolslDataDTO;
import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import org.springframework.stereotype.Component;

@Component
public class PlanPolslDataDTOMapper implements ModelMapper<PlanPolslData, PlanPolslDataDTO> {

    @Override
    public PlanPolslDataDTO map(PlanPolslData input) {
        return PlanPolslDataDTO.builder()
                .id(input.getId())
                .type(input.getType())
                .weekDays(input.getWeekDays())
                .build();
    }

}
