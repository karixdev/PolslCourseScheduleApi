package com.github.karixdev.webscraperservice.model;

import java.util.Set;

public record PlanPolslResponse(
        Set<TimeCell> timeCells,
        Set<CourseCell> courseCells
) {}
