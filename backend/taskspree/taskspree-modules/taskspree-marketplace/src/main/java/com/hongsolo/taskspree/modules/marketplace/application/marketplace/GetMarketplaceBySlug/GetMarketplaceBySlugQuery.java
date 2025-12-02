package com.hongsolo.taskspree.modules.marketplace.application.marketplace.GetMarketplaceBySlug;

import com.hongsolo.taskspree.common.application.cqrs.Query;
import com.hongsolo.taskspree.common.domain.Result;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GetMarketplaceBySlugQuery(
        @NotBlank(message = "Slug is required")
        String slug,

        @NotNull(message = "User ID is required")
        UUID userId
) implements Query<Result<MarketplaceDetailResponse>> {
}

