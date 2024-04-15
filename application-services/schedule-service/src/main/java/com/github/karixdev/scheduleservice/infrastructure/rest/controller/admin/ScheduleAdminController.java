package com.github.karixdev.scheduleservice.infrastructure.rest.controller.admin;

import com.github.karixdev.scheduleservice.application.command.BlankSchedulesUpdateCommand;
import com.github.karixdev.scheduleservice.application.command.CreateScheduleCommand;
import com.github.karixdev.scheduleservice.application.command.DeleteScheduleByIdCommand;
import com.github.karixdev.scheduleservice.application.command.UpdateScheduleByIdCommand;
import com.github.karixdev.scheduleservice.application.command.handler.CommandHandler;
import com.github.karixdev.scheduleservice.application.dto.ScheduleDTO;
import com.github.karixdev.scheduleservice.application.filter.PlanPolslDataFilter;
import com.github.karixdev.scheduleservice.application.filter.ScheduleFilter;
import com.github.karixdev.scheduleservice.application.mapper.ModelMapper;
import com.github.karixdev.scheduleservice.application.pagination.Page;
import com.github.karixdev.scheduleservice.application.pagination.PageRequest;
import com.github.karixdev.scheduleservice.application.query.QueryHandler;
import com.github.karixdev.scheduleservice.application.query.admin.FindScheduleByFilterAndPaginationQuery;
import com.github.karixdev.scheduleservice.infrastructure.rest.params.ScheduleFilterParams;
import com.github.karixdev.scheduleservice.infrastructure.rest.payload.ScheduleRequest;
import com.github.karixdev.scheduleservice.infrastructure.rest.payload.response.ScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/schedules")
@RequiredArgsConstructor
public class ScheduleAdminController {

    private final CommandHandler<CreateScheduleCommand> createScheduleCommandHandler;
    private final CommandHandler<DeleteScheduleByIdCommand> deleteScheduleByIdCommandHandler;
    private final CommandHandler<UpdateScheduleByIdCommand> updateScheduleByIdCommandHandler;
    private final CommandHandler<BlankSchedulesUpdateCommand> blankSchedulesUpdateCommandHandler;

    private final QueryHandler<FindScheduleByFilterAndPaginationQuery, Page<ScheduleDTO>> findScheduleByFilterAndPaginationQueryHandler;

    private final ModelMapper<ScheduleDTO, ScheduleResponse> scheduleResponseMapper;

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

    @PutMapping("/blank-update")
    ResponseEntity<Void> blankUpdateSchedulesByIds(
            @RequestParam("id") List<UUID> ids
    ) {
        BlankSchedulesUpdateCommand command = new BlankSchedulesUpdateCommand(ids);
        blankSchedulesUpdateCommandHandler.handle(command);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    ResponseEntity<Page<ScheduleResponse>> findByFilterWithPagination(
            ScheduleFilterParams scheduleFilterParams,

            @RequestParam(value = "plan-polsl-id", required = false) List<Integer> planPolslId,
            @RequestParam(value = "plan-polsl-type", required = false) List<Integer> planPolslType,
            @RequestParam(value = "plan-polsl-week-days", required = false) List<Integer> planPolslWeekDays,

            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {
        ScheduleFilter filter = ScheduleFilter.builder()
                .ids(scheduleFilterParams.id())
                .majors(scheduleFilterParams.major())
                .semesters(scheduleFilterParams.semester())
                .groups(scheduleFilterParams.group())
                .planPolslDataFilter(
                        PlanPolslDataFilter.builder()
                                .ids(planPolslId)
                                .types(planPolslType)
                                .weedDays(planPolslWeekDays)
                                .build()
                )
                .build();
        PageRequest pageRequest = new PageRequest(page, size);

        FindScheduleByFilterAndPaginationQuery query = new FindScheduleByFilterAndPaginationQuery(filter, pageRequest);
        Page<ScheduleDTO> scheduleDTOPage = findScheduleByFilterAndPaginationQueryHandler.handle(query);

        List<ScheduleResponse> mappedContent = scheduleDTOPage.content()
                .stream()
                .map(scheduleResponseMapper::map)
                .toList();

        Page<ScheduleResponse> finalPage = Page.<ScheduleResponse>builder()
                .pageInfo(scheduleDTOPage.pageInfo())
                .content(mappedContent)
                .build();

        return ResponseEntity.ok(finalPage);
    }

}
