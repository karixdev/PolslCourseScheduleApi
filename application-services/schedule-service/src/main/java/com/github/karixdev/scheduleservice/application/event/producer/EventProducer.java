package com.github.karixdev.scheduleservice.application.event.producer;

public interface EventProducer<T> {
    void produce(T event);
}
