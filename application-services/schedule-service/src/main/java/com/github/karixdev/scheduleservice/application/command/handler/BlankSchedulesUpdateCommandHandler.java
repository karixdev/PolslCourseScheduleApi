package com.github.karixdev.scheduleservice.application.command.handler;

import com.github.karixdev.scheduleservice.application.command.BlankSchedulesUpdateCommand;
import com.github.karixdev.scheduleservice.application.exception.NoBlankScheduleUpdateStrategyException;
import com.github.karixdev.scheduleservice.application.strategy.blankupdate.BlankSchedulesUpdateStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BlankSchedulesUpdateCommandHandler implements CommandHandler<BlankSchedulesUpdateCommand> {

    private final List<BlankSchedulesUpdateStrategy> strategies;

    @Override
    public void handle(BlankSchedulesUpdateCommand command) {
        BlankSchedulesUpdateStrategy strategy = strategies.stream()
                .filter(s -> s.supports(command.ids()))
                .findFirst()
                .orElseThrow(NoBlankScheduleUpdateStrategyException::new);

        strategy.blankUpdate(command.ids());
    }
}
