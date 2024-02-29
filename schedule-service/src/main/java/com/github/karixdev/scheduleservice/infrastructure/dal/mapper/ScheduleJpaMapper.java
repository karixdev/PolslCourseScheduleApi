package com.github.karixdev.scheduleservice.infrastructure.dal.mapper;

import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.infrastructure.dal.entity.ScheduleEntity;
import org.springframework.stereotype.Component;

@Component
public class ScheduleJpaMapper {

    public ScheduleEntity toJpaEntity(Schedule domainSchedule) {
        return ScheduleEntity.builder()
                .id(domainSchedule.getId())
                .name(domainSchedule.getName())
                .semester(domainSchedule.getSemester())
                .groupNumber(domainSchedule.getGroupNumber())
                .type(domainSchedule.getPlanPolslData().getType())
                .planPolslId(domainSchedule.getPlanPolslData().getId())
                .wd(domainSchedule.getPlanPolslData().getWeekDays())
                .build();
    }

    public Schedule toDomainEntity(ScheduleEntity jpaSchedule) {
        return Schedule.builder()
                .id(jpaSchedule.getId())
                .name(jpaSchedule.getName())
                .semester(jpaSchedule.getSemester())
                .groupNumber(jpaSchedule.getGroupNumber())
                .planPolslData(
                        PlanPolslData.builder()
                                .id(jpaSchedule.getPlanPolslId())
                                .type(jpaSchedule.getType())
                                .weekDays(jpaSchedule.getWd())
                                .build()
                )
                .build();
    }

}
