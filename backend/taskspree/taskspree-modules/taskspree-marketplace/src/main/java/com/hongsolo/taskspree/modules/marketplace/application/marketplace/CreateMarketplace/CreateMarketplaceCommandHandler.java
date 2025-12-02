package com.hongsolo.taskspree.modules.marketplace.application.marketplace.CreateMarketplace;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.repository.IMarketplaceRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.member.MarketplaceMember;
import com.hongsolo.taskspree.modules.marketplace.domain.member.repository.IMarketplaceMemberRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.role.MarketplaceRole;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;
import com.hongsolo.taskspree.modules.marketplace.domain.role.repository.IMarketplaceRoleRepository;
import com.hongsolo.taskspree.modules.marketplace.infrastructure.utils.SlugGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateMarketplaceCommandHandler
        implements CommandHandler<CreateMarketplaceCommand, Result<CreateMarketplaceResponse>> {

    private final IMarketplaceRepository marketplaceRepository;
    private final IMarketplaceRoleRepository marketplaceRoleRepository;
    private final IMarketplaceMemberRepository marketplaceMemberRepository;

    @Override
    @Transactional
    public Result<CreateMarketplaceResponse> handle(CreateMarketplaceCommand command) {
        log.info("Creating marketplace '{}' for owner: {}", command.name(), command.ownerId());

        // 1. Generate unique slug
        String baseSlug = SlugGenerator.generate(command.name());
        String slug = ensureUniqueSlug(baseSlug);

        // 2. Create marketplace
        Marketplace marketplace = Marketplace.create(
                command.ownerId(),
                command.name(),
                slug
        );

        if (command.description() != null && !command.description().isBlank()) {
            marketplace.updateInfo(command.name(), command.description());
        }

        marketplace = marketplaceRepository.save(marketplace);
        log.debug("Created marketplace with ID: {}", marketplace.getId());

        // 3. Create default roles
        MarketplaceRole ownerRole = MarketplaceRole.create(marketplace, RoleType.OWNER);
        MarketplaceRole managerRole = MarketplaceRole.create(marketplace, RoleType.MANAGER);
        MarketplaceRole memberRole = MarketplaceRole.create(marketplace, RoleType.MEMBER);

        marketplaceRoleRepository.saveAllRoles(List.of(ownerRole, managerRole, memberRole));
        log.debug("Created default roles for marketplace: {}", marketplace.getId());

        // 4. Add owner as member
        MarketplaceMember ownerMember = MarketplaceMember.create(
                marketplace,
                command.ownerId(),
                ownerRole
        );
        marketplaceMemberRepository.save(ownerMember);

        log.info("Marketplace created successfully: {} ({})", marketplace.getName(), slug);

        return Result.success(CreateMarketplaceResponse.of(
                marketplace.getId(),
                marketplace.getName(),
                marketplace.getSlug()
        ));
    }

    private String ensureUniqueSlug(String baseSlug) {
        String slug = baseSlug;
        int attempt = 0;

        while (marketplaceRepository.existsBySlug(slug)) {
            attempt++;
            String suffix = UUID.randomUUID().toString().substring(0, 6);
            slug = SlugGenerator.generateUnique(baseSlug, suffix);

            if (attempt > 10) {
                slug = baseSlug + "-" + UUID.randomUUID();
                break;
            }
        }

        return slug;
    }
}
