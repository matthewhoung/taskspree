package com.hongsolo.taskspree.modules.marketplace.application.member.GetMarketplaceInvites;

import com.hongsolo.taskspree.common.application.cqrs.Query;
import com.hongsolo.taskspree.common.domain.Result;

import java.util.List;
import java.util.UUID;

public record GetMarketplaceInvitesQuery(
        UUID marketplaceId,
        UUID userId
) implements Query<Result<List<InviteSummaryResponse>>> {
}
