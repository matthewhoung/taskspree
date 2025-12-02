package com.hongsolo.taskspree.modules.marketplace.application.role.UpdateRoleDisplayName;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateRoleDisplayNameCommand(
        @NotNull(message = "Marketplace ID is required")
        UUID marketplaceId,

        @NotNull(message = "Role type is required")
        RoleType roleType,

        @NotBlank(message = "Display name is required")
        @Size(min = 2, max = 50, message = "Display name must be between 2 and 50 characters")
        String displayName,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        @NotNull(message = "User ID is required")
        UUID userId
) implements Command<Result<Void>> {
}
