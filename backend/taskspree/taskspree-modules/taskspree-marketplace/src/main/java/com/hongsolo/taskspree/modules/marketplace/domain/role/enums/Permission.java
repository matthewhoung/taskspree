package com.hongsolo.taskspree.modules.marketplace.domain.role.enums;

public enum Permission {
    MANAGE_MARKETPLACE("Edit marketplace settings and configure roles"),
    MANAGE_MEMBERS("Invite and remove members"),
    PUBLISH_TASK("Create and publish tasks"),
    EDIT_ANY_TASK("Modify any task in the marketplace"),
    CLOSE_TASK("Review and close completed tasks"),
    VIEW_ANALYTICS("View marketplace statistics and analytics");

    private final String description;

    Permission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}