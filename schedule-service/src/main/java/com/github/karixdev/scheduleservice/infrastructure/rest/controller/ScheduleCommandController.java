package com.github.karixdev.scheduleservice.infrastructure.rest.controller;

import com.github.karixdev.scheduleservice.application.command.CreateScheduleCommand;
import com.github.karixdev.scheduleservice.application.command.DeleteScheduleByIdCommand;
import com.github.karixdev.scheduleservice.application.command.UpdateScheduleByIdCommand;
import com.github.karixdev.scheduleservice.application.command.handler.CommandHandler;
import com.github.karixdev.scheduleservice.infrastructure.rest.payload.ScheduleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/commands/schedules")
@RequiredArgsConstructor
public class ScheduleCommandController {

    private final CommandHandler<CreateScheduleCommand> createScheduleCommandHandler;
    private final CommandHandler<DeleteScheduleByIdCommand> deleteScheduleByIdCommandHandler;
    private final CommandHandler<UpdateScheduleByIdCommand> updateScheduleByIdCommandHandler;

    @PostMapping
    ResponseEntity<Void> createSchedule(@RequestBody ScheduleRequest payload) {
        CreateScheduleCommand command = CreateScheduleCommand.builder()
                .type(payload.type())
                .planPolslId(payload.planPolslId())
                .semester(payload.semester())
                .semester(payload.semester())
                .major(payload.major())
                .groupNumber(payload.groupNumber())
                .weekDays(payload.wd())
                .build();

        createScheduleCommandHandler.handle(command);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteScheduleById(@PathVariable UUID id) {
        DeleteScheduleByIdCommand command = DeleteScheduleByIdCommand.builder()
                .id(id)
                .build();

        deleteScheduleByIdCommandHandler.handle(command);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    ResponseEntity<Void> updateScheduleById(
            @PathVariable UUID id,
            @RequestBody ScheduleRequest payload
    ) {
        UpdateScheduleByIdCommand command = UpdateScheduleByIdCommand.builder()
                .id(id)
                .type(payload.type())
                .planPolslId(payload.planPolslId())
                .semester(payload.semester())
                .major(payload.major())
                .groupNumber(payload.groupNumber())
                .weekDays(payload.wd())
                .build();

        updateScheduleByIdCommandHandler.handle(command);

        return ResponseEntity.noContent().build();
    }

}
