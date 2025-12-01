package com.hongsolo.taskspree.modules.marketplace.domain.marketplace.enums;

public enum MarketplaceStatus {
    ACTIVE("Marketplace is active and operational"),
    ARCHIVED("Marketplace has been archived");

    private final String description;

    MarketplaceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}