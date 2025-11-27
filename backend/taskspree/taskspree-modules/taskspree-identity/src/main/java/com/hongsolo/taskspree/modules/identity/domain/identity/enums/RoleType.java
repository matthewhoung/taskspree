package com.hongsolo.taskspree.modules.identity.domain.identity.enums;

public enum RoleType {
    ADMIN("Systems Administrator"),
    USER("Application User");

    private final String description;

    RoleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}