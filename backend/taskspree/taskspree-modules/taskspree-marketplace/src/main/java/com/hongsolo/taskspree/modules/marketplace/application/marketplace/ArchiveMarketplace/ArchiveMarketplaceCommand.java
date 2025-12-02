package com.hongsolo.taskspree.modules.marketplace.application.marketplace.ArchiveMarketplace;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ArchiveMarketplaceCommand(
        @NotNull(message = "Marketplace ID is required")
        UUID marketplaceId,

        @NotNull(message = "User ID is required")
        UUID userId
) implements Command<Result<Void>> {
}
