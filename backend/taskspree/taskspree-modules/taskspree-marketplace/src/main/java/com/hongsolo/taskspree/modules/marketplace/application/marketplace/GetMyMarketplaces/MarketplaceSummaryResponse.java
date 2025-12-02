package com.hongsolo.taskspree.modules.marketplace.application.marketplace.GetMyMarketplaces;

import java.util.UUID;

public record MarketplaceSummaryResponse (
        UUID id,
        String name,
        String slug,
        String description,
        UUID logoFileId,
        String status,
        String userRole,
        String roleDisplayName,
        boolean isOwner,
        long memberCount
) {
}
