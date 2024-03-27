package com.github.karixdev.scheduleservice.application.command.handler;

import com.github.karixdev.scheduleservice.application.command.UpdateScheduleByIdCommand;
import com.github.karixdev.scheduleservice.application.dal.TransactionManager;
import com.github.karixdev.scheduleservice.application.event.EventType;
import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.application.event.producer.EventProducer;
import com.github.karixdev.scheduleservice.application.exception.ScheduleWithIdNotFoundException;
import com.github.karixdev.scheduleservice.application.exception.UnavailablePlanPolslIdException;
import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UpdateScheduleByIdCommandHandler implements CommandHandler<UpdateScheduleByIdCommand> {

    private final ScheduleRepository repository;
    private final TransactionManager transactionManager;
    private final EventProducer<ScheduleEvent> producer;

    @Override
    public void handle(UpdateScheduleByIdCommand command) {
        Optional<Schedule> optionalSchedule = repository.findById(command.id());
        if (optionalSchedule.isEmpty()) {
            throw new ScheduleWithIdNotFoundException(command.id());
        }

        Schedule schedule = optionalSchedule.get();

        if (repository.findByPlanPolslId(command.planPolslId()).isPresent()) {
            throw new UnavailablePlanPolslIdException(command.planPolslId());
        }

        PlanPolslData currentPlanPolslData = schedule.getPlanPolslData();

        PlanPolslData updatedPlanPolslData = PlanPolslData.builder()
                .id(command.planPolslId())
                .type(command.type())
                .weekDays(command.weekDays())
                .build();

        boolean shouldProduceEvent = shouldProduceEvent(currentPlanPolslData, updatedPlanPolslData);

        if (!isPlanPolslIdAvailable(currentPlanPolslData, updatedPlanPolslData)) {
            throw new UnavailablePlanPolslIdException(command.planPolslId());
        }

        schedule.setPlanPolslData(updatedPlanPolslData);
        schedule.setSemester(command.semester());
        schedule.setMajor(command.major());
        schedule.setGroupNumber(command.groupNumber());

        transactionManager.execute(() -> repository.save(schedule));

        if (shouldProduceEvent) {
            ScheduleEvent event = ScheduleEvent.builder()
                    .type(EventType.UPDATE)
                    .scheduleId(schedule.getId().toString())
                    .entity(schedule)
                    .build();

            producer.produce(event);
        }
    }

    private boolean shouldProduceEvent(PlanPolslData currentData, PlanPolslData newData) {
        return !Objects.equals(currentData.getType(), newData.getType())
                || !Objects.equals(currentData.getId(), newData.getId())
                || !Objects.equals(currentData.getWeekDays(), newData.getWeekDays());
    }

    private boolean isPlanPolslIdAvailable(PlanPolslData currentData, PlanPolslData newData) {
        return Objects.equals(currentData.getId(), newData.getId())
                || repository.findByPlanPolslId(newData.getId()).isEmpty();
    }

}