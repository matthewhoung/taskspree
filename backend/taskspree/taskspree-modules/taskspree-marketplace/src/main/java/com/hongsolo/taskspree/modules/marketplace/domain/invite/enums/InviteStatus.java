package com.hongsolo.taskspree.modules.marketplace.domain.invite.enums;

public enum InviteStatus {
    PENDING("Invite is pending acceptance"),
    ACCEPTED("Invite has been accepted"),
    DECLINED("Invite has been declined"),
    CANCELLED("Invite has been cancelled by inviter"),
    EXPIRED("Invite has expired");

    private final String description;

    InviteStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isTerminal() {
        return this == ACCEPTED || this == DECLINED || this == CANCELLED || this == EXPIRED;
    }
}