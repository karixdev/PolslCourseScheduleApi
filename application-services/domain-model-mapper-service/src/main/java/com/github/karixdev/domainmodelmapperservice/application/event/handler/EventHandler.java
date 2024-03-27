package com.github.karixdev.domainmodelmapperservice.application.event.handler;

public interface EventHandler<T> {
    void handle(T event);
}
