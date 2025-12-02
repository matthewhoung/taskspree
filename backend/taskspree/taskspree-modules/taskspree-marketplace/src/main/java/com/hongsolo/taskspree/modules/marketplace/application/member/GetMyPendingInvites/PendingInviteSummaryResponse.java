package com.hongsolo.taskspree.modules.marketplace.application.member.GetMyPendingInvites;

import java.time.Instant;
import java.util.UUID;

public record PendingInviteSummaryResponse(
        UUID inviteId,
        String token,
        UUID marketplaceId,
        String marketplaceName,
        String marketplaceSlug,
        String roleType,
        String roleDisplayName,
        String inviterEmail,
        Instant expiresAt,
        Instant createdAt
) {
}
