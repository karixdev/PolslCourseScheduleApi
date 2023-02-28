package com.github.karixdev.webscraperservice.planpolsl.domain;

import java.util.Set;

public record PlanPolslResponse(
        Set<TimeCell> timeCells,
        Set<CourseCell> courseCells
) {}
