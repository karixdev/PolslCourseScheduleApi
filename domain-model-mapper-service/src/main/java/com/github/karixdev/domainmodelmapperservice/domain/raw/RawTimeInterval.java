package com.github.karixdev.domainmodelmapperservice.domain.raw;

import lombok.Builder;

@Builder
public record RawTimeInterval(String start, String end) {}
