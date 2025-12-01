package com.hongsolo.taskspree.modules.marketplace.infrastructure.member;

import com.hongsolo.taskspree.modules.marketplace.domain.member.MarketplaceMember;
import com.hongsolo.taskspree.modules.marketplace.domain.member.enums.MemberStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.member.repository.IMarketplaceMemberRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketplaceMemberRepository
        extends JpaRepository<MarketplaceMember, UUID>, IMarketplaceMemberRepository {

    Optional<MarketplaceMember> findByMarketplace_IdAndUserId(UUID marketplaceId, UUID userId);

    Optional<MarketplaceMember> findByMarketplace_IdAndUserIdAndStatus(UUID marketplaceId, UUID userId, MemberStatus status);

    List<MarketplaceMember> findByMarketplace_Id(UUID marketplaceId);

    List<MarketplaceMember> findByMarketplace_IdAndStatus(UUID marketplaceId, MemberStatus status);

    List<MarketplaceMember> findByUserId(UUID userId);

    List<MarketplaceMember> findByUserIdAndStatus(UUID userId, MemberStatus status);

    boolean existsByMarketplace_IdAndUserId(UUID marketplaceId, UUID userId);

    boolean existsByMarketplace_IdAndUserIdAndStatus(UUID marketplaceId, UUID userId, MemberStatus status);

    long countByMarketplace_IdAndStatus(UUID marketplaceId, MemberStatus status);

    @Query("""
            SELECT m FROM MarketplaceMember m
            WHERE m.marketplace.id = :marketplaceId
            AND m.role.roleType = 'OWNER'
            AND m.status = 'ACTIVE'
            """)
    Optional<MarketplaceMember> findOwnerByMarketplace_Id(@Param("marketplaceId") UUID marketplaceId);

    @Query("""
            SELECT m FROM MarketplaceMember m
            WHERE m.marketplace.id = :marketplaceId
            AND m.role.roleType = :roleType
            AND m.status = 'ACTIVE'
            """)
    List<MarketplaceMember> findByMarketplace_IdAndRole_RoleType(
            @Param("marketplaceId") UUID marketplaceId,
            @Param("roleType") RoleType roleType
    );

    // ==================== Default Method Bridges ====================

    @Override
    default Optional<MarketplaceMember> findByMarketplaceIdAndUserId(UUID marketplaceId, UUID userId) {
        return findByMarketplace_IdAndUserId(marketplaceId, userId);
    }

    @Override
    default Optional<MarketplaceMember> findByMarketplaceIdAndUserIdAndStatus(UUID marketplaceId, UUID userId, MemberStatus status) {
        return findByMarketplace_IdAndUserIdAndStatus(marketplaceId, userId, status);
    }

    @Override
    default List<MarketplaceMember> findByMarketplaceId(UUID marketplaceId) {
        return findByMarketplace_Id(marketplaceId);
    }

    @Override
    default List<MarketplaceMember> findByMarketplaceIdAndStatus(UUID marketplaceId, MemberStatus status) {
        return findByMarketplace_IdAndStatus(marketplaceId, status);
    }

    @Override
    default boolean existsByMarketplaceIdAndUserId(UUID marketplaceId, UUID userId) {
        return existsByMarketplace_IdAndUserId(marketplaceId, userId);
    }

    @Override
    default boolean existsByMarketplaceIdAndUserIdAndStatus(UUID marketplaceId, UUID userId, MemberStatus status) {
        return existsByMarketplace_IdAndUserIdAndStatus(marketplaceId, userId, status);
    }

    @Override
    default long countByMarketplaceIdAndStatus(UUID marketplaceId, MemberStatus status) {
        return countByMarketplace_IdAndStatus(marketplaceId, status);
    }

    @Override
    default Optional<MarketplaceMember> findOwnerByMarketplaceId(UUID marketplaceId) {
        return findOwnerByMarketplace_Id(marketplaceId);
    }

    @Override
    default List<MarketplaceMember> findByMarketplaceIdAndRoleType(UUID marketplaceId, RoleType roleType) {
        return findByMarketplace_IdAndRole_RoleType(marketplaceId, roleType);
    }
}