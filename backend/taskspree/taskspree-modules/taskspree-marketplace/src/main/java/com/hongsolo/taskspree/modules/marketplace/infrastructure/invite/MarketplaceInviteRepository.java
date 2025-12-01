package com.hongsolo.taskspree.modules.marketplace.infrastructure.invite;

import com.hongsolo.taskspree.modules.marketplace.domain.invite.MarketplaceInvite;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.enums.InviteStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.repository.IMarketplaceInviteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketplaceInviteRepository
        extends JpaRepository<MarketplaceInvite, UUID>, IMarketplaceInviteRepository {

    Optional<MarketplaceInvite> findByToken(String token);

    List<MarketplaceInvite> findByMarketplace_Id(UUID marketplaceId);

    List<MarketplaceInvite> findByMarketplace_IdAndStatus(UUID marketplaceId, InviteStatus status);

    List<MarketplaceInvite> findByInviteeUserId(UUID inviteeUserId);

    List<MarketplaceInvite> findByInviteeUserIdAndStatus(UUID inviteeUserId, InviteStatus status);

    Optional<MarketplaceInvite> findByMarketplace_IdAndInviteeUserIdAndStatus(
            UUID marketplaceId,
            UUID inviteeUserId,
            InviteStatus status
    );

    boolean existsByMarketplace_IdAndInviteeUserIdAndStatus(
            UUID marketplaceId,
            UUID inviteeUserId,
            InviteStatus status
    );

    boolean existsByToken(String token);

    @Query("""
            SELECT i FROM MarketplaceInvite i
            WHERE i.status = 'PENDING'
            AND i.expiresAt < CURRENT_TIMESTAMP
            ORDER BY i.expiresAt ASC
            """)
    List<MarketplaceInvite> findExpiredPendingInvitesQuery(PageRequest pageRequest);

    // ==================== Default Method Bridges ====================

    @Override
    default List<MarketplaceInvite> findByMarketplaceId(UUID marketplaceId) {
        return findByMarketplace_Id(marketplaceId);
    }

    @Override
    default List<MarketplaceInvite> findByMarketplaceIdAndStatus(UUID marketplaceId, InviteStatus status) {
        return findByMarketplace_IdAndStatus(marketplaceId, status);
    }

    @Override
    default Optional<MarketplaceInvite> findByMarketplaceIdAndInviteeUserIdAndStatus(
            UUID marketplaceId,
            UUID inviteeUserId,
            InviteStatus status
    ) {
        return findByMarketplace_IdAndInviteeUserIdAndStatus(marketplaceId, inviteeUserId, status);
    }

    @Override
    default boolean existsByMarketplaceIdAndInviteeUserIdAndStatus(
            UUID marketplaceId,
            UUID inviteeUserId,
            InviteStatus status
    ) {
        return existsByMarketplace_IdAndInviteeUserIdAndStatus(marketplaceId, inviteeUserId, status);
    }

    @Override
    default List<MarketplaceInvite> findExpiredPendingInvites(int limit) {
        return findExpiredPendingInvitesQuery(PageRequest.of(0, limit));
    }
}