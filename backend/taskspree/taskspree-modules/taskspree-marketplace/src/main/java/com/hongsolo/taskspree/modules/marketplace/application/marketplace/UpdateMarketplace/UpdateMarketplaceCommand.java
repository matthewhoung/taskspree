package com.hongsolo.taskspree.modules.marketplace.application.marketplace.UpdateMarketplace;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateMarketplaceCommand(
        @NotNull(message = "Marketplace ID is required")
        UUID marketplaceId,

        @NotNull(message = "User ID is required")
        UUID userId,

        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        // Settings (nullable means no change)
        Integer defaultTaskDurationDays,
        Integer autoCloseSlotsPercentage,
        Integer reservationTimeoutDays
) implements Command<Result<Void>> {
}
