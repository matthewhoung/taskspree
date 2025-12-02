package com.hongsolo.taskspree.modules.marketplace.application.member.AcceptInvite;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AcceptInviteCommand(
        @NotBlank(message = "Token is required")
        String token,

        @NotNull(message = "User ID is required")
        UUID userId
) implements Command<Result<AcceptInviteResponse>> {
}
