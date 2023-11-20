package com.github.karixdev.webscraperservice.model;

import com.github.karixdev.commonservice.model.course.raw.TimeCell;
import com.github.karixdev.commonservice.model.course.raw.CourseCell;

import java.util.Set;

public record PlanPolslResponse(
        Set<TimeCell> timeCells,
        Set<CourseCell> courseCells
) {}
