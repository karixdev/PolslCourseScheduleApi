package com.github.karixdev.courseservice.application.validator;

public interface Validator<E> {
    boolean isValid(E model);
}
