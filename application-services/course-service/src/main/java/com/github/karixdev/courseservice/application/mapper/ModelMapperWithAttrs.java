package com.github.karixdev.courseservice.application.mapper;

import java.util.Map;

public interface ModelMapperWithAttrs<I, O> {
    O map(I input, Map<String, Object> attrs);
}
