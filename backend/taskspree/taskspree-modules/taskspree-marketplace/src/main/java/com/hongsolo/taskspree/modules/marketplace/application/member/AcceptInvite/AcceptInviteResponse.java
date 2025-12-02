package com.hongsolo.taskspree.modules.marketplace.application.member.AcceptInvite;

import java.util.UUID;

public record AcceptInviteResponse(
        UUID memberId,
        UUID marketplaceId,
        String marketplaceName,
        String marketplaceSlug,
        String roleType,
        String roleDisplayName,
        String message
) {
    public static AcceptInviteResponse of(
            UUID memberId,
            UUID marketplaceId,
            String marketplaceName,
            String marketplaceSlug,
            String roleType,
            String roleDisplayName
    ) {
        return new AcceptInviteResponse(
                memberId,
                marketplaceId,
                marketplaceName,
                marketplaceSlug,
                roleType,
                roleDisplayName,
                "You have joined the marketplace successfully"
        );
    }
}
