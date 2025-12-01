package com.hongsolo.taskspree.common.application.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Facade pattern for marketplace-related operations.
 * This abstraction allows modules to interact with marketplace data without
 * directly depending on the Marketplace module.
 */
public interface IMarketplaceFacadeService {

    // === Permission Checks ===

    /**
     * Check if user has a specific permission in a marketplace
     */
    boolean hasPermission(UUID userId, UUID marketplaceId, String permission);

    /**
     * Check if user is a member of a marketplace
     */
    boolean isMember(UUID userId, UUID marketplaceId);

    /**
     * Check if user is the owner of a marketplace
     */
    boolean isOwner(UUID userId, UUID marketplaceId);

    // === Queries ===

    /**
     * Find marketplace by ID
     */
    Optional<MarketplaceDto> findById(UUID marketplaceId);

    /**
     * Find marketplace by slug
     */
    Optional<MarketplaceDto> findBySlug(String slug);

    /**
     * Find member information
     */
    Optional<MemberDto> findMember(UUID userId, UUID marketplaceId);

    /**
     * Find all marketplaces owned by a user
     */
    List<MarketplaceDto> findByOwnerId(UUID ownerId);

    /**
     * Find all marketplaces a user is a member of
     */
    List<MarketplaceDto> findByMemberId(UUID userId);

    // === Commands ===

    /**
     * Create a default marketplace for a new user (used during signup)
     */
    UUID createDefaultMarketplace(CreateDefaultMarketplaceCommand command);

    // === DTOs ===

    record MarketplaceDto(
            UUID id,
            String name,
            String slug,
            UUID ownerId,
            String status
    ) {}

    record MemberDto(
            UUID memberId,
            UUID userId,
            UUID roleId,
            String roleType,
            String roleDisplayName,
            List<String> permissions
    ) {}

    record CreateDefaultMarketplaceCommand(
            UUID ownerId,
            String name
    ) {}
}