package com.github.karixdev.webscraperservice.domain;

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
