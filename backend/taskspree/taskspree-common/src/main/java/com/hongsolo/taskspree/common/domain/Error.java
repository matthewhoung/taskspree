package com.hongsolo.taskspree.common.domain;

public record Error(String code, String description, ErrorType type) {
    public static final Error NONE = new Error("", "", ErrorType.FAILURE);
    public static final Error NULL_VALUE = new Error("Error.NullValue", "Null value was provided", ErrorType.FAILURE);

    public enum ErrorType {
        FAILURE,
        VALIDATION,
        NOT_FOUND,
        CONFLICT
    }
}