package com.github.karixdev.webscraperservice.domain;

import lombok.Builder;

@Builder
public record RawAnchor(String address, String text) {}
