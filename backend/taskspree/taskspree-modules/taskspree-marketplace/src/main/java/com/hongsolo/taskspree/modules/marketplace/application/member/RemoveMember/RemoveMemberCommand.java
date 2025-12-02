package com.hongsolo.taskspree.modules.marketplace.application.member.RemoveMember;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RemoveMemberCommand(
        @NotNull(message = "Marketplace ID is required")
        UUID marketplaceId,

        @NotNull(message = "Member user ID to remove is required")
        UUID memberUserId,

        @NotNull(message = "Requester user ID is required")
        UUID requesterId
) implements Command<Result<Void>> {
}
