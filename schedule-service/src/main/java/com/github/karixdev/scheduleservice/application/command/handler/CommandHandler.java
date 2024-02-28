package com.github.karixdev.scheduleservice.application.command.handler;

public interface CommandHandler<T> {
    void handle(T command);
}
