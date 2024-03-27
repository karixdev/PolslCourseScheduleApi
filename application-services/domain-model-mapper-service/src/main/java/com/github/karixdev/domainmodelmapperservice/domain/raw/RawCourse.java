package com.github.karixdev.domainmodelmapperservice.domain.raw;

import lombok.Builder;

import java.util.Set;

@Builder
public record RawCourse(
        String text,
        Integer height,
        Integer width,
        Integer left,
        Integer top,
        Set<RawAnchor> anchors
) {}
