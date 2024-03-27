package com.github.karixdev.commonservice.model.course.raw;

import lombok.Builder;

import java.util.HashSet;
import java.util.Objects;
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

    public CourseCell(int top, int left, int ch, int cw, String text, Set<Link> links) {
        this.top = top;
        this.left = left;
        this.ch = ch;
        this.cw = cw;
        this.text = text;
        this.links = Objects.requireNonNullElseGet(links, HashSet::new);
    }
}
