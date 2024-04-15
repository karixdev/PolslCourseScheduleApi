package com.github.karixdev.courseservice.application.mapper;

public interface ModelMapper<I, O> {
    O map(I input);
}
