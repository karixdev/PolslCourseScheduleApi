package com.github.karixdev.webscraperservice.application.event.producer;

public interface EventProducer<T> {
    void produce(T event);
}
