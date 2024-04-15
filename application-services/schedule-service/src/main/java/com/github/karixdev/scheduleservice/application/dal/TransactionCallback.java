package com.github.karixdev.scheduleservice.application.dal;

@FunctionalInterface
public interface TransactionCallback {

    void execute();

}
