package com.hongsolo.taskspree.modules.marketplace.application.member.InviteMember;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InviteMemberCommand(
        @NotNull(message = "Marketplace ID is required")
        UUID marketplaceId,

        @NotNull(message = "Inviter user ID is required")
        UUID inviterUserId,

        @NotBlank(message = "Invitee email is required")
        @Email(message = "Invalid email format")
        String inviteeEmail,

        @NotNull(message = "Role type is required")
        RoleType roleType
) implements Command<Result<InviteMemberResponse>> {
}
