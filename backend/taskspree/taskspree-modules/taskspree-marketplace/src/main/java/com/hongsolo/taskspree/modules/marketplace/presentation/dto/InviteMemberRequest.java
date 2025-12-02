package com.hongsolo.taskspree.modules.marketplace.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteMemberRequest(
        @NotBlank(message = "Invitee email is required")
        @Email(message = "Invalid email format")
        String inviteeEmail,

        @NotBlank(message = "Role type is required")
        String roleType
) {
}
