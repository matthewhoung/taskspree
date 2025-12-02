package com.hongsolo.taskspree.modules.marketplace.application.marketplace.GetMarketplaceBySlug;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MarketplaceDetailResponse(
        UUID id,
        String name,
        String slug,
        String description,
        UUID logoFileId,
        String status,
        UUID ownerId,

        // Settings
        SettingsDto settings,

        // User's context in this marketplace
        UserContextDto userContext,

        // Timestamps
        Instant createdAt,
        Instant updatedAt
) {
    public record SettingsDto(
            int defaultTaskDurationDays,
            int autoCloseSlotsPercentage,
            int reservationTimeoutDays
    ) {
    }

    public record UserContextDto(
            UUID memberId,
            String roleType,
            String roleDisplayName,
            List<String> permissions,
            boolean isOwner
    ) {
    }
}
