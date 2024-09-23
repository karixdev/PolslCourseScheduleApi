package com.github.karixdev.scheduleservice.commons.vo.pagination;

import lombok.Builder;

@Builder
public record PageInfo(
        Integer page,
        Integer size,
        Integer numberOfElements,
        Long totalElements,
        Integer totalPages,
        Boolean isLast
) {}
