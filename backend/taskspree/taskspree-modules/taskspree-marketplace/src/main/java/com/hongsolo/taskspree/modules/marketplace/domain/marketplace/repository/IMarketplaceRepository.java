package com.hongsolo.taskspree.modules.marketplace.domain.marketplace.repository;

import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.enums.MarketplaceStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IMarketplaceRepository {

    Marketplace save(Marketplace marketplace);

    Optional<Marketplace> findById(UUID id);

    Optional<Marketplace> findBySlug(String slug);

    List<Marketplace> findByOwnerId(UUID ownerId);

    List<Marketplace> findByOwnerIdAndStatus(UUID ownerId, MarketplaceStatus status);

    boolean existsBySlug(String slug);

    boolean existsByOwnerIdAndStatus(UUID ownerId, MarketplaceStatus status);

    long countByOwnerId(UUID ownerId);

    long countByOwnerIdAndStatus(UUID ownerId, MarketplaceStatus status);
}