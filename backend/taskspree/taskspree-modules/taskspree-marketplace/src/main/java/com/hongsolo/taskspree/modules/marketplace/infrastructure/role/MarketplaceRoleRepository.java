package com.hongsolo.taskspree.modules.marketplace.infrastructure.role;

import com.hongsolo.taskspree.modules.marketplace.domain.role.MarketplaceRole;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;
import com.hongsolo.taskspree.modules.marketplace.domain.role.repository.IMarketplaceRoleRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketplaceRoleRepository
        extends JpaRepository<MarketplaceRole, UUID>, IMarketplaceRoleRepository {

    Optional<MarketplaceRole> findByMarketplace_IdAndRoleType(UUID marketplaceId, RoleType roleType);

    List<MarketplaceRole> findByMarketplace_Id(UUID marketplaceId);

    boolean existsByMarketplace_IdAndRoleType(UUID marketplaceId, RoleType roleType);

    // ==================== Default Method Bridges ====================

    @Override
    default List<MarketplaceRole> saveAllRoles(Iterable<MarketplaceRole> roles) {
        return saveAll(roles);
    }

    @Override
    default Optional<MarketplaceRole> findByMarketplaceIdAndRoleType(UUID marketplaceId, RoleType roleType) {
        return findByMarketplace_IdAndRoleType(marketplaceId, roleType);
    }

    @Override
    default List<MarketplaceRole> findByMarketplaceId(UUID marketplaceId) {
        return findByMarketplace_Id(marketplaceId);
    }

    @Override
    default boolean existsByMarketplaceIdAndRoleType(UUID marketplaceId, RoleType roleType) {
        return existsByMarketplace_IdAndRoleType(marketplaceId, roleType);
    }
}