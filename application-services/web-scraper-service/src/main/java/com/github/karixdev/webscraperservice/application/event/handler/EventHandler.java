package com.github.karixdev.webscraperservice.application.event.handler;

public interface EventHandler<T> {
    void handle(T event);
}
