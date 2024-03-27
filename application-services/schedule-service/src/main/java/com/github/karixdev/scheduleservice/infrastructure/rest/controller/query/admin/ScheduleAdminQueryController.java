package com.github.karixdev.scheduleservice.infrastructure.rest.controller.query.admin;

import com.github.karixdev.scheduleservice.application.dto.ScheduleDTO;
import com.github.karixdev.scheduleservice.application.filter.PlanPolslDataFilter;
import com.github.karixdev.scheduleservice.application.filter.ScheduleFilter;
import com.github.karixdev.scheduleservice.application.mapper.ModelMapper;
import com.github.karixdev.scheduleservice.application.pagination.Page;
import com.github.karixdev.scheduleservice.application.pagination.PageRequest;
import com.github.karixdev.scheduleservice.application.query.QueryHandler;
import com.github.karixdev.scheduleservice.application.query.admin.FindScheduleByFilterAndPaginationQuery;
import com.github.karixdev.scheduleservice.infrastructure.rest.params.ScheduleFilterParams;
import com.github.karixdev.scheduleservice.infrastructure.rest.payload.response.ScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/queries/schedules")
@RequiredArgsConstructor
public class ScheduleAdminQueryController {

    private final QueryHandler<FindScheduleByFilterAndPaginationQuery, Page<ScheduleDTO>> findScheduleByFilterAndPaginationQueryHandler;

    private final ModelMapper<ScheduleDTO, ScheduleResponse> scheduleResponseMapper;

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
