package com.github.karixdev.webscraperservice.event.producer;

public interface EventProducer<T> {
    void produce(T event);
}
