package com.hongsolo.taskspree.modules.marketplace.domain.member.enums;

public enum MemberStatus {
    ACTIVE("Member is active"),
    REMOVED("Member has been removed");

    private final String description;

    MemberStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}