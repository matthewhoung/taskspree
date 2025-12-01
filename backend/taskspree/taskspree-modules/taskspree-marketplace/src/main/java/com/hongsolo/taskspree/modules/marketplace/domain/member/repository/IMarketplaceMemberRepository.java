package com.hongsolo.taskspree.modules.marketplace.domain.member.repository;

import com.hongsolo.taskspree.modules.marketplace.domain.member.MarketplaceMember;
import com.hongsolo.taskspree.modules.marketplace.domain.member.enums.MemberStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IMarketplaceMemberRepository {

    MarketplaceMember save(MarketplaceMember member);

    Optional<MarketplaceMember> findById(UUID id);

    Optional<MarketplaceMember> findByMarketplaceIdAndUserId(UUID marketplaceId, UUID userId);

    Optional<MarketplaceMember> findByMarketplaceIdAndUserIdAndStatus(UUID marketplaceId, UUID userId, MemberStatus status);

    List<MarketplaceMember> findByMarketplaceId(UUID marketplaceId);

    List<MarketplaceMember> findByMarketplaceIdAndStatus(UUID marketplaceId, MemberStatus status);

    List<MarketplaceMember> findByUserId(UUID userId);

    List<MarketplaceMember> findByUserIdAndStatus(UUID userId, MemberStatus status);

    boolean existsByMarketplaceIdAndUserId(UUID marketplaceId, UUID userId);

    boolean existsByMarketplaceIdAndUserIdAndStatus(UUID marketplaceId, UUID userId, MemberStatus status);

    long countByMarketplaceIdAndStatus(UUID marketplaceId, MemberStatus status);

    /**
     * Find the owner member of a marketplace
     */
    Optional<MarketplaceMember> findOwnerByMarketplaceId(UUID marketplaceId);

    /**
     * Find member by marketplace and role type
     */
    List<MarketplaceMember> findByMarketplaceIdAndRoleType(UUID marketplaceId, RoleType roleType);
}