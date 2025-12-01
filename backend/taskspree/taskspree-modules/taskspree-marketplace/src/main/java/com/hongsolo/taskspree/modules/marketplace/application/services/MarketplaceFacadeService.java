package com.hongsolo.taskspree.modules.marketplace.application.services;

import com.hongsolo.taskspree.common.application.services.IMarketplaceFacadeService;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.enums.MarketplaceStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.repository.IMarketplaceRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.member.MarketplaceMember;
import com.hongsolo.taskspree.modules.marketplace.domain.member.enums.MemberStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.member.repository.IMarketplaceMemberRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.role.MarketplaceRole;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.Permission;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;
import com.hongsolo.taskspree.modules.marketplace.domain.role.repository.IMarketplaceRoleRepository;
import com.hongsolo.taskspree.modules.marketplace.infrastructure.utils.SlugGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketplaceFacadeService implements IMarketplaceFacadeService {

    private final IMarketplaceRepository marketplaceRepository;
    private final IMarketplaceRoleRepository marketplaceRoleRepository;
    private final IMarketplaceMemberRepository marketplaceMemberRepository;

    // === Permission Checks ===

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(UUID userId, UUID marketplaceId, String permission) {
        try {
            Permission perm = Permission.valueOf(permission);
            return marketplaceMemberRepository
                    .findByMarketplaceIdAndUserIdAndStatus(marketplaceId, userId, MemberStatus.ACTIVE)
                    .map(member -> member.hasPermission(perm))
                    .orElse(false);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid permission: {}", permission);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMember(UUID userId, UUID marketplaceId) {
        return marketplaceMemberRepository
                .existsByMarketplaceIdAndUserIdAndStatus(marketplaceId, userId, MemberStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOwner(UUID userId, UUID marketplaceId) {
        return marketplaceRepository.findById(marketplaceId)
                .map(mp -> mp.isOwner(userId))
                .orElse(false);
    }

    // === Queries ===

    @Override
    @Transactional(readOnly = true)
    public Optional<MarketplaceDto> findById(UUID marketplaceId) {
        return marketplaceRepository.findById(marketplaceId)
                .map(this::toMarketplaceDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MarketplaceDto> findBySlug(String slug) {
        return marketplaceRepository.findBySlug(slug)
                .map(this::toMarketplaceDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MemberDto> findMember(UUID userId, UUID marketplaceId) {
        return marketplaceMemberRepository
                .findByMarketplaceIdAndUserIdAndStatus(marketplaceId, userId, MemberStatus.ACTIVE)
                .map(this::toMemberDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarketplaceDto> findByOwnerId(UUID ownerId) {
        return marketplaceRepository.findByOwnerIdAndStatus(ownerId, MarketplaceStatus.ACTIVE)
                .stream()
                .map(this::toMarketplaceDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarketplaceDto> findByMemberId(UUID userId) {
        return marketplaceMemberRepository.findByUserIdAndStatus(userId, MemberStatus.ACTIVE)
                .stream()
                .map(member -> member.getMarketplace())
                .filter(mp -> mp.getStatus() == MarketplaceStatus.ACTIVE)
                .map(this::toMarketplaceDto)
                .toList();
    }

    // === Commands ===

    @Override
    @Transactional
    public UUID createDefaultMarketplace(CreateDefaultMarketplaceCommand command) {
        log.info("Creating default marketplace for user: {}", command.ownerId());

        // 1. Generate slug from name
        String baseSlug = SlugGenerator.generate(command.name());
        String slug = ensureUniqueSlug(baseSlug);

        // 2. Create marketplace
        Marketplace marketplace = Marketplace.create(
                command.ownerId(),
                command.name(),
                slug
        );
        marketplace = marketplaceRepository.save(marketplace);

        log.debug("Created marketplace with ID: {}", marketplace.getId());

        // 3. Create default roles (OWNER, MANAGER, MEMBER)
        MarketplaceRole ownerRole = MarketplaceRole.create(marketplace, RoleType.OWNER);
        MarketplaceRole managerRole = MarketplaceRole.create(marketplace, RoleType.MANAGER);
        MarketplaceRole memberRole = MarketplaceRole.create(marketplace, RoleType.MEMBER);

        marketplaceRoleRepository.saveAllRoles(List.of(ownerRole, managerRole, memberRole));

        log.debug("Created default roles for marketplace: {}", marketplace.getId());

        // 4. Add owner as member with OWNER role
        MarketplaceMember ownerMember = MarketplaceMember.create(
                marketplace,
                command.ownerId(),
                ownerRole
        );
        marketplaceMemberRepository.save(ownerMember);

        log.info("Default marketplace created successfully: {} ({})", marketplace.getName(), marketplace.getSlug());

        return marketplace.getId();
    }

    // === Helper Methods ===

    private String ensureUniqueSlug(String baseSlug) {
        String slug = baseSlug;
        int attempt = 0;

        while (marketplaceRepository.existsBySlug(slug)) {
            attempt++;
            // Append a short unique suffix
            String suffix = UUID.randomUUID().toString().substring(0, 6);
            slug = SlugGenerator.generateUnique(baseSlug, suffix);

            if (attempt > 10) {
                // Fallback to full UUID
                slug = baseSlug + "-" + UUID.randomUUID();
                break;
            }
        }

        return slug;
    }

    private MarketplaceDto toMarketplaceDto(Marketplace marketplace) {
        return new MarketplaceDto(
                marketplace.getId(),
                marketplace.getName(),
                marketplace.getSlug(),
                marketplace.getOwnerId(),
                marketplace.getStatus().name()
        );
    }

    private MemberDto toMemberDto(MarketplaceMember member) {
        MarketplaceRole role = member.getRole();
        return new MemberDto(
                member.getId(),
                member.getUserId(),
                role.getId(),
                role.getRoleType().name(),
                role.getDisplayName(),
                role.getPermissions().stream()
                        .map(Permission::name)
                        .toList()
        );
    }
}