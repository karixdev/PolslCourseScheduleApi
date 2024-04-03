package com.github.karixdev.courseservice.application.event.handler;

public interface EventHandler<E> {
    void handle(E event);
}
