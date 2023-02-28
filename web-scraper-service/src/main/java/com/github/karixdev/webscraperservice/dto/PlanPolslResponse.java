package com.github.karixdev.webscraperservice.dto;

import java.util.Set;

public record PlanPolslResponse(
        Set<TimeCell> timeCells,
        Set<CourseCell> courseCells
) {}
