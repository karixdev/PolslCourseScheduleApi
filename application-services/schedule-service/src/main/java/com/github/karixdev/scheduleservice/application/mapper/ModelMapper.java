package com.github.karixdev.scheduleservice.application.mapper;

public interface ModelMapper<I, R> {
    R map(I input);
}
