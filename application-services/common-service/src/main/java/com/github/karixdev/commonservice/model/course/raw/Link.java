package com.github.karixdev.commonservice.model.course.raw;

import lombok.Builder;

@Builder
public record Link(
        String text,
        String href
) {}
