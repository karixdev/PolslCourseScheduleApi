package com.github.karixdev.scheduleservice.application.mapper;

import com.github.karixdev.scheduleservice.application.dto.PlanPolslDataDTO;
import com.github.karixdev.scheduleservice.application.dto.ScheduleDTO;
import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleDTOMapper implements ModelMapper<Schedule, ScheduleDTO> {

    private final ModelMapper<PlanPolslData, PlanPolslDataDTO> planPolslDataDTOMapper;

    @Override
    public ScheduleDTO map(Schedule input) {
        PlanPolslDataDTO planPolslDataDTO = planPolslDataDTOMapper.map(input.getPlanPolslData());
        return ScheduleDTO.builder()
                .id(input.getId())
                .major(input.getMajor())
                .semester(input.getSemester())
                .groupNumber(input.getGroupNumber())
                .planPolslData(planPolslDataDTO)
                .build();
    }
}
