package com.hongsolo.taskspree.modules.marketplace.application.member.InviteMember;

import java.time.Instant;
import java.util.UUID;

public record InviteMemberResponse(
        UUID inviteId,
        UUID marketplaceId,
        String inviteeEmail,
        String roleType,
        String roleDisplayName,
        Instant expiresAt,
        String message
) {
    public static InviteMemberResponse of(
            UUID inviteId,
            UUID marketplaceId,
            String inviteeEmail,
            String roleType,
            String roleDisplayName,
            Instant expiresAt
    ) {
        return new InviteMemberResponse(
                inviteId,
                marketplaceId,
                inviteeEmail,
                roleType,
                roleDisplayName,
                expiresAt,
                "Invitation sent successfully"
        );
    }
}
