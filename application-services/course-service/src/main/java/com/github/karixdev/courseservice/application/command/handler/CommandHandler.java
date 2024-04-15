package com.github.karixdev.courseservice.application.command.handler;

public interface CommandHandler<C> {
    void handle(C command);
}
