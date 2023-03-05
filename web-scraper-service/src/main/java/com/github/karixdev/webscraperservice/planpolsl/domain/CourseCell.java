package com.github.karixdev.webscraperservice.planpolsl.domain;

import java.util.HashSet;
import java.util.Set;

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
