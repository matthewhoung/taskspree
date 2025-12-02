package com.hongsolo.taskspree.modules.marketplace.presentation.dto;

public record UpdateMarketplaceRequest(
        String name,
        String description,
        Integer defaultTaskDurationDays,
        Integer autoCloseSlotsPercentage,
        Integer reservationTimeoutDays
) {
}
