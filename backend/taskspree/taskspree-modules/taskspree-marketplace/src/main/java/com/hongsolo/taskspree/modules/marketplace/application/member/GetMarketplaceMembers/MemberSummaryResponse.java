package com.hongsolo.taskspree.modules.marketplace.application.member.GetMarketplaceMembers;

import java.time.Instant;
import java.util.UUID;

public record MemberSummaryResponse(
        UUID memberId,
        UUID userId,
        String roleType,
        String roleDisplayName,
        boolean isOwner,
        Instant joinedAt
) {
}
