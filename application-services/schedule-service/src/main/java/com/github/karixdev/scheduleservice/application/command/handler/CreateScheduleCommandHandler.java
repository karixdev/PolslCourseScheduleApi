package com.github.karixdev.scheduleservice.application.command.handler;

import com.github.karixdev.scheduleservice.application.command.CreateScheduleCommand;
import com.github.karixdev.scheduleservice.application.dal.TransactionManager;
import com.github.karixdev.scheduleservice.application.event.EventType;
import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.application.event.producer.EventProducer;
import com.github.karixdev.scheduleservice.application.exception.UnavailablePlanPolslIdException;
import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateScheduleCommandHandler implements CommandHandler<CreateScheduleCommand> {

    private final ScheduleRepository repository;
    private final TransactionManager transactionManager;
    private final EventProducer<ScheduleEvent> producer;

    @Override
    public void handle(CreateScheduleCommand command) {
        if (repository.findByPlanPolslId(command.planPolslId()).isPresent()) {
            throw new UnavailablePlanPolslIdException(command.planPolslId());
        }

        PlanPolslData planPolslData = PlanPolslData.builder()
                .id(command.planPolslId())
                .type(command.type())
                .weekDays(command.weekDays())
                .build();

        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(command.semester())
                .major(command.major())
                .groupNumber(command.groupNumber())
                .planPolslData(planPolslData)
                .build();

        transactionManager.execute(() -> repository.save(schedule));

        ScheduleEvent event = ScheduleEvent.builder()
                .type(EventType.CREATE)
                .scheduleId(schedule.getId().toString())
                .entity(schedule)
                .build();

        producer.produce(event);
    }

}
