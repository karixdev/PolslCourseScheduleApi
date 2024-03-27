package com.github.karixdev.domainmodelmapperservice.domain.processed;

import lombok.Builder;

import java.time.LocalTime;

@Builder
public record ProcessedRawTimeInterval(LocalTime start, LocalTime end) {}
