package com.github.karixdev.scheduleservice.infrastructure.rest.controller;

import com.github.karixdev.scheduleservice.application.command.CreateScheduleCommand;
import com.github.karixdev.scheduleservice.application.command.handler.CommandHandler;
import com.github.karixdev.scheduleservice.infrastructure.rest.controller.payload.ScheduleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commands/schedules")
@RequiredArgsConstructor
public class ScheduleCommandController {

    private final CommandHandler<CreateScheduleCommand> createScheduleCommandHandler;

    @PostMapping
    ResponseEntity<Void> createSchedule(@RequestBody ScheduleRequest payload) {
        CreateScheduleCommand command = CreateScheduleCommand.builder()
                .type(payload.type())
                .planPolslId(payload.planPolslId())
                .semester(payload.semester())
                .semester(payload.semester())
                .name(payload.name())
                .groupNumber(payload.groupNumber())
                .wd(payload.wd())
                .build();

        createScheduleCommandHandler.handle(command);

        return ResponseEntity.noContent().build();
    }

}
