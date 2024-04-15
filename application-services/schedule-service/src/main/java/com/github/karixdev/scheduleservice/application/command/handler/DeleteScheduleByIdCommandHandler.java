package com.github.karixdev.scheduleservice.application.command.handler;

import com.github.karixdev.scheduleservice.application.command.DeleteScheduleByIdCommand;
import com.github.karixdev.scheduleservice.application.dal.TransactionManager;
import com.github.karixdev.scheduleservice.application.event.EventType;
import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.application.event.producer.EventProducer;
import com.github.karixdev.scheduleservice.application.exception.ScheduleWithIdNotFoundException;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeleteScheduleByIdCommandHandler implements CommandHandler<DeleteScheduleByIdCommand> {

    private final ScheduleRepository repository;
    private final TransactionManager transactionManager;
    private final EventProducer<ScheduleEvent> producer;

    @Override
    public void handle(DeleteScheduleByIdCommand command) {
        Optional<Schedule> optionalSchedule = repository.findById(command.id());
        if (optionalSchedule.isEmpty()) {
            throw new ScheduleWithIdNotFoundException(command.id());
        }

        Schedule schedule = optionalSchedule.get();
        transactionManager.execute(() -> repository.delete(schedule));

        ScheduleEvent scheduleEvent = ScheduleEvent.builder()
                .type(EventType.DELETE)
                .scheduleId(schedule.getId().toString())
                .entity(schedule)
                .build();

        producer.produce(scheduleEvent);
    }

}
