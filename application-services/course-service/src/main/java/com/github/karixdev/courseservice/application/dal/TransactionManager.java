package com.github.karixdev.courseservice.application.dal;

public interface TransactionManager {
    void execute(TransactionCallback callback);
}
