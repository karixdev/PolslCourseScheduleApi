package com.github.karixdev.scheduleservice.application.query.user.handler;

public interface QueryHandler<Q, R> {
    R handle(Q q);
}
