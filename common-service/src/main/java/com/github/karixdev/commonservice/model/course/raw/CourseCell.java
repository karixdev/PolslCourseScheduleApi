package com.github.karixdev.commonservice.model.course.raw;

import lombok.Builder;

import java.util.HashSet;
import java.util.Set;

@Builder
public record CourseCell(
        int top,
        int left,
        int ch,
        int cw,
        String text,
        Set<Link> links
) {
    public CourseCell(int top, int left, int ch, int cw, String text) {
        this(top, left, ch, cw, text, new HashSet<>());
    }
}
