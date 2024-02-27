package com.github.karixdev.webscraperservice.domain;

import lombok.Builder;

@Builder
public record Schedule(String id, int type, Integer planPolslId, Integer wd) {}
