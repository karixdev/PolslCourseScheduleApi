package com.github.karixdev.scheduleservice.application.pagination;

import lombok.Builder;

import java.util.List;

@Builder
public record Page<T>(List<T> content, PageInfo pageInfo) {}
