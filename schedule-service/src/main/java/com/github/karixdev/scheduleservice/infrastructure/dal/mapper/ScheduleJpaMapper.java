package com.github.karixdev.scheduleservice.infrastructure.dal.mapper;

import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.infrastructure.dal.entity.ScheduleEntity;
import org.springframework.stereotype.Component;

@Component
public class ScheduleJpaMapper {

    public ScheduleEntity toJpaEntity(Schedule domainSchedule) {
        return ScheduleEntity.builder()
                .id(domainSchedule.getId())
                .semester(domainSchedule.getSemester())
                .groupNumber(domainSchedule.getGroupNumber())
                .type(domainSchedule.getType())
                .planPolslId(domainSchedule.getPlanPolslId())
                .wd(domainSchedule.getWd())
                .build();
    }

    public Schedule toDomainEntity(ScheduleEntity jpaSchedule) {
        return Schedule.builder()
                .id(jpaSchedule.getId())
                .semester(jpaSchedule.getSemester())
                .groupNumber(jpaSchedule.getGroupNumber())
                .type(jpaSchedule.getType())
                .planPolslId(jpaSchedule.getPlanPolslId())
                .wd(jpaSchedule.getWd())
                .build();
    }

}
