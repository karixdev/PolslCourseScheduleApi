package com.github.karixdev.scheduleservice.application.command.handler;

import com.github.karixdev.scheduleservice.application.command.CreateScheduleCommand;
import com.github.karixdev.scheduleservice.application.dal.TransactionManager;
import com.github.karixdev.scheduleservice.application.event.EventType;
import com.github.karixdev.scheduleservice.application.event.ScheduleEvent;
import com.github.karixdev.scheduleservice.application.event.producer.EventProducer;
import com.github.karixdev.scheduleservice.application.exception.UnavailableScheduleNameException;
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
        if (repository.findByName(command.name()).isPresent()) {
            throw new UnavailableScheduleNameException(command.name());
        }

        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID())
                .semester(command.semester())
                .name(command.name())
                .groupNumber(command.groupNumber())
                .type(command.type())
                .planPolslId(command.planPolslId())
                .wd(command.wd())
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
