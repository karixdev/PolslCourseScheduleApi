package com.github.karixdev.scheduleservice.infrastructure.rest.params;

import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public record PlanPolslDataFilterParams(
        @RequestParam("plan-polsl-id")
        List<Integer> planPolslId,

        @RequestParam("plan-polsl-type")
        List<Integer> planPolslType,

        @RequestParam("plan-polsl-week-days")
        List<Integer> planPolslWeekDays
) {}
