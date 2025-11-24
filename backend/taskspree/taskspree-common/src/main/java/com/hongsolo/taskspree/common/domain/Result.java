package com.hongsolo.taskspree.common.domain;

import java.util.Objects;

// Java 21 Sealed Interface
public sealed interface Result<T> {

    boolean isSuccess();
    Error error();

    // Success Implementation
    record Success<T>(T value) implements Result<T> {
        @Override public boolean isSuccess() { return true; }
        @Override public Error error() { return Error.NONE; }
    }

    // Failure Implementation
    record Failure<T>(Error error) implements Result<T> {
        public Failure { Objects.requireNonNull(error); }
        @Override public boolean isSuccess() { return false; }
        // This method is needed to satisfy the interface contract if you ever call .value() on a failure
        // But ideally, you switch on the type.
    }

    // Static Factories
    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> failure(Error error) {
        return new Failure<>(error);
    }
}