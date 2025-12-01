package com.hongsolo.taskspree.modules.marketplace.infrastructure.marketplace;

import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.enums.MarketplaceStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.repository.IMarketplaceRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketplaceRepository
        extends JpaRepository<Marketplace, UUID>, IMarketplaceRepository {

    Optional<Marketplace> findBySlug(String slug);

    List<Marketplace> findByOwnerId(UUID ownerId);

    List<Marketplace> findByOwnerIdAndStatus(UUID ownerId, MarketplaceStatus status);

    boolean existsBySlug(String slug);

    boolean existsByOwnerIdAndStatus(UUID ownerId, MarketplaceStatus status);

    long countByOwnerId(UUID ownerId);

    long countByOwnerIdAndStatus(UUID ownerId, MarketplaceStatus status);
}