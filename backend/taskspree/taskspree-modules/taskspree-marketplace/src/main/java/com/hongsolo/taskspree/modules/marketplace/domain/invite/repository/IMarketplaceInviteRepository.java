package com.hongsolo.taskspree.modules.marketplace.domain.invite.repository;

import com.hongsolo.taskspree.modules.marketplace.domain.invite.MarketplaceInvite;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.enums.InviteStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IMarketplaceInviteRepository {

    MarketplaceInvite save(MarketplaceInvite invite);

    Optional<MarketplaceInvite> findById(UUID id);

    Optional<MarketplaceInvite> findByToken(String token);

    List<MarketplaceInvite> findByMarketplaceId(UUID marketplaceId);

    List<MarketplaceInvite> findByMarketplaceIdAndStatus(UUID marketplaceId, InviteStatus status);

    List<MarketplaceInvite> findByInviteeUserId(UUID inviteeUserId);

    List<MarketplaceInvite> findByInviteeUserIdAndStatus(UUID inviteeUserId, InviteStatus status);

    Optional<MarketplaceInvite> findByMarketplaceIdAndInviteeUserIdAndStatus(
            UUID marketplaceId,
            UUID inviteeUserId,
            InviteStatus status
    );

    boolean existsByMarketplaceIdAndInviteeUserIdAndStatus(
            UUID marketplaceId,
            UUID inviteeUserId,
            InviteStatus status
    );

    boolean existsByToken(String token);

    /**
     * Find expired pending invites for cleanup
     */
    List<MarketplaceInvite> findExpiredPendingInvites(int limit);
}