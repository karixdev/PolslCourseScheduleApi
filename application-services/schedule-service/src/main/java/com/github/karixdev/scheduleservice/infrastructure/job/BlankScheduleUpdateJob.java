package com.github.karixdev.scheduleservice.infrastructure.job;

import com.github.karixdev.scheduleservice.application.command.BlankSchedulesUpdateCommand;
import com.github.karixdev.scheduleservice.application.command.handler.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BlankScheduleUpdateJob {

    private final CommandHandler<BlankSchedulesUpdateCommand> blankSchedulesUpdateCommandHandler;

    @Scheduled(cron = "${schedule.job.cron}")
    private void updateScheduleCourses() {
        BlankSchedulesUpdateCommand command = new BlankSchedulesUpdateCommand(List.of());
        blankSchedulesUpdateCommandHandler.handle(command);
    }

}
