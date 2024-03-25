package com.github.karixdev.webscraperservice.domain;

import lombok.Builder;

@Builder
public record PlanPolslData(
    Integer id,
    Integer type,
    Integer weekDays
) {}
