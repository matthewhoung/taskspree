package com.hongsolo.taskspree.modules.marketplace.application.member.GetMyPendingInvites;

import com.hongsolo.taskspree.common.application.cqrs.Query;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record GetMyPendingInvitesQuery(
        @NotNull(message = "User ID is required")
        UUID userId
) implements Query<List<PendingInviteSummaryResponse>> {
}
