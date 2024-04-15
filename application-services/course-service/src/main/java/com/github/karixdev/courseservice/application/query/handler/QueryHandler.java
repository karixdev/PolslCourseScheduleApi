package com.github.karixdev.courseservice.application.query.handler;

public interface QueryHandler<Q, O> {
    O handle(Q query);
}
