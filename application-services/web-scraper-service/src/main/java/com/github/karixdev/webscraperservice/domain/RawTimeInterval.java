package com.github.karixdev.webscraperservice.domain;

import lombok.Builder;

@Builder
public record RawTimeInterval(String start, String end) {}
