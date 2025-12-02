package com.hongsolo.taskspree.modules.marketplace.application.member.TransferOwnership;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransferOwnershipCommand(
        @NotNull(message = "Marketplace ID is required")
        UUID marketplaceId,

        @NotNull(message = "New owner user ID is required")
        UUID newOwnerUserId,

        @NotNull(message = "Current owner user ID is required")
        UUID currentOwnerUserId
) implements Command<Result<Void>> {
}
