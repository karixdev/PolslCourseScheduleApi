package com.github.karixdev.scheduleservice.application.mapper;

import com.github.karixdev.scheduleservice.application.dto.PublicScheduleDTO;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import org.springframework.stereotype.Component;

@Component
public class PublicScheduleDTOMapper implements ModelMapper<Schedule, PublicScheduleDTO> {

    @Override
    public PublicScheduleDTO map(Schedule input) {
        return PublicScheduleDTO.builder()
                .id(input.getId())
                .group(input.getGroupNumber())
                .build();
    }

}
