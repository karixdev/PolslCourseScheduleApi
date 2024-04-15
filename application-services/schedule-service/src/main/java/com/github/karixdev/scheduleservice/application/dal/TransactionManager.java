package com.github.karixdev.scheduleservice.application.dal;

public interface TransactionManager {
    void execute(TransactionCallback callback);
}
