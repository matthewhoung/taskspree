package com.hongsolo.taskspree.modules.marketplace.application.member.GetMarketplaceInvites;

import java.time.Instant;
import java.util.UUID;

public record InviteSummaryResponse(
        UUID inviteId,
        String inviteeEmail,
        String roleType,
        String roleDisplayName,
        String status,
        boolean isExpired,
        String inviterEmail,
        Instant expiresAt,
        Instant createdAt,
        Instant respondedAt
) {
}
