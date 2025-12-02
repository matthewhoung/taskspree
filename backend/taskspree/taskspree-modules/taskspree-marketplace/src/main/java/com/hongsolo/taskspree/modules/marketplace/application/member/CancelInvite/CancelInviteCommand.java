package com.hongsolo.taskspree.modules.marketplace.application.member.CancelInvite;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CancelInviteCommand(
        @NotNull(message = "Invite ID is required")
        UUID inviteId,

        @NotNull(message = "User ID is required")
        UUID userId
) implements Command<Result<Void>> {
}
