package com.hongsolo.taskspree.modules.marketplace.application.marketplace.CreateMarketplace;

import java.util.UUID;

public record CreateMarketplaceResponse(
        UUID marketplaceId,
        String name,
        String slug,
        String message
) {
    public static CreateMarketplaceResponse of(UUID marketplaceId, String name, String slug) {
        return new CreateMarketplaceResponse(
                marketplaceId,
                name,
                slug,
                "Marketplace created successfully"
        );
    }
}
