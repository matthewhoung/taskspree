package com.hongsolo.taskspree.modules.marketplace.domain.role.repository;

import com.hongsolo.taskspree.modules.marketplace.domain.role.MarketplaceRole;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IMarketplaceRoleRepository {

    MarketplaceRole save(MarketplaceRole role);

    List<MarketplaceRole> saveAllRoles(Iterable<MarketplaceRole> roles);

    Optional<MarketplaceRole> findById(UUID id);

    Optional<MarketplaceRole> findByMarketplaceIdAndRoleType(UUID marketplaceId, RoleType roleType);

    List<MarketplaceRole> findByMarketplaceId(UUID marketplaceId);

    boolean existsByMarketplaceIdAndRoleType(UUID marketplaceId, RoleType roleType);
}