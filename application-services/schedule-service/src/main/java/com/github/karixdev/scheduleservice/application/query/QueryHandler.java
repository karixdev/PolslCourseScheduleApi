package com.github.karixdev.scheduleservice.application.query;

public interface QueryHandler<Q, R> {
    R handle(Q q);
}
