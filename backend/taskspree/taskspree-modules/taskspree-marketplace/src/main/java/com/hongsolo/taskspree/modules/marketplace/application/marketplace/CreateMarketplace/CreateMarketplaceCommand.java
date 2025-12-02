package com.hongsolo.taskspree.modules.marketplace.application.marketplace.CreateMarketplace;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateMarketplaceCommand(
        @NotNull(message = "Owner ID is required")
        UUID ownerId,

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description
) implements Command<Result<CreateMarketplaceResponse>> {
}
