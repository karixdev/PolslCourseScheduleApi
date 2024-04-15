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
import com.github.karixdev.scheduleservice.infrastructure.rest.exception.handler.payload.ValidationErrorResponse;
import com.github.karixdev.scheduleservice.infrastructure.rest.params.ScheduleFilterParams;
import com.github.karixdev.scheduleservice.infrastructure.rest.payload.ScheduleRequest;
import com.github.karixdev.scheduleservice.infrastructure.rest.payload.response.ScheduleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Admin schedule actions")
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

    @Operation(summary = "Creates new schedule")
    @ApiResponse(
            responseCode = "204",
            description = "No content"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))
    )
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

    @Operation(summary = "Deletes schedule by id")
    @ApiResponse(
            responseCode = "204",
            description = "No content"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))
    )
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteScheduleById(@PathVariable UUID id) {
        DeleteScheduleByIdCommand command = DeleteScheduleByIdCommand.builder()
                .id(id)
                .build();

        deleteScheduleByIdCommandHandler.handle(command);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Updates whole already existing schedule by id")
    @ApiResponse(
            responseCode = "204",
            description = "No content"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))
    )
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

    @Operation(summary = "Perform blank update on schedules with provided ids")
    @ApiResponse(
            responseCode = "204",
            description = "No content"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))
    )
    @PutMapping("/blank-update")
    ResponseEntity<Void> blankUpdateSchedulesByIds(
            @RequestParam("id") List<UUID> ids
    ) {
        BlankSchedulesUpdateCommand command = new BlankSchedulesUpdateCommand(ids);
        blankSchedulesUpdateCommandHandler.handle(command);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Gets all schedules that satisfy filter")
    @ApiResponse(
            responseCode = "200",
            description = "Ok"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(hidden = true))
    )
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(schema = @Schema(hidden = true))
    )
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
