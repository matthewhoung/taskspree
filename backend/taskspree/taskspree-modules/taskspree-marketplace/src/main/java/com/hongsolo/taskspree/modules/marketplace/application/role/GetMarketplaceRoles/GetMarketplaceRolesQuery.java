package com.hongsolo.taskspree.modules.marketplace.application.role.GetMarketplaceRoles;

import com.hongsolo.taskspree.common.application.cqrs.Query;
import com.hongsolo.taskspree.common.domain.Result;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record GetMarketplaceRolesQuery(
        @NotNull(message = "Marketplace ID is required")
        UUID marketplaceId,

        @NotNull(message = "User ID is required")
        UUID userId
) implements Query<Result<List<RoleSummaryResponse>>> {
}
