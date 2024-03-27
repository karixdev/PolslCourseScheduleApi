package com.github.karixdev.domainmodelmapperservice.domain.raw;
import lombok.Builder;

import java.util.Set;

@Builder
public record RawSchedule(Set<RawTimeInterval> timeIntervals, Set<RawCourse> courses) {}