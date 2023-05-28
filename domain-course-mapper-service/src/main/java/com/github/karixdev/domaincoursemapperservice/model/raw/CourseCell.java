package com.github.karixdev.domaincoursemapperservice.model.raw;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

public record CourseCell(
        @JsonProperty("top")
        int top,
        @JsonProperty("left")
        int left,
        @JsonProperty("ch")
        int ch,
        @JsonProperty("cw")
        int cw,
        @JsonProperty("text")
        String text,
        @JsonProperty("links")
        Set<Link> links
) {
    public CourseCell(int top, int left, int ch, int cw, String text) {
        this(top, left, ch, cw, text, new HashSet<>());
    }
}
