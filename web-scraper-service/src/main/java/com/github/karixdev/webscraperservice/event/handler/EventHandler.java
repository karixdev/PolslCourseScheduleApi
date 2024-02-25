package com.github.karixdev.webscraperservice.event.handler;

public interface EventHandler<T> {
    void handle(T event);
}
