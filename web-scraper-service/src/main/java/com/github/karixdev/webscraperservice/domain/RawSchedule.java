package com.github.karixdev.webscraperservice.domain;

import com.github.karixdev.webscraperservice.domain.exception.EmptyRawCourseSetException;
import com.github.karixdev.webscraperservice.domain.exception.EmptyRawTimeIntervalSetException;
import lombok.Builder;

import java.util.Set;

@Builder
public record RawSchedule(Set<RawTimeInterval> timeIntervals, Set<RawCourse> courses) {

    public RawSchedule {
        if (timeIntervals.isEmpty()) {
            throw new EmptyRawTimeIntervalSetException();
        }

        if (courses.isEmpty()) {
            throw new EmptyRawCourseSetException();
        }
    }

}
