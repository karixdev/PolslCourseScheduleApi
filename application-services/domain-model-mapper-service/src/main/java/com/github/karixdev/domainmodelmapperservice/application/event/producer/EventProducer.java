package com.github.karixdev.domainmodelmapperservice.application.event.producer;

public interface EventProducer<T> {
    void produce(T event);
}
