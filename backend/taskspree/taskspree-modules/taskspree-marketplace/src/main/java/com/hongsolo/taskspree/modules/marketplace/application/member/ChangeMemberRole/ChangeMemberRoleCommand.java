package com.hongsolo.taskspree.modules.marketplace.application.member.ChangeMemberRole;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChangeMemberRoleCommand(
        @NotNull(message = "Marketplace ID is required")
        UUID marketplaceId,

        @NotNull(message = "Member user ID is required")
        UUID memberUserId,

        @NotNull(message = "New role type is required")
        RoleType newRoleType,

        @NotNull(message = "Requester user ID is required")
        UUID requesterId
) implements Command<Result<Void>> {
}
