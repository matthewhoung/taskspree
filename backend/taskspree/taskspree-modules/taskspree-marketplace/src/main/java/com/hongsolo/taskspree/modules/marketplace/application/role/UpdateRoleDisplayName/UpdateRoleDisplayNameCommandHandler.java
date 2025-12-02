package com.hongsolo.taskspree.modules.marketplace.application.role.UpdateRoleDisplayName;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.MarketplaceErrors;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.repository.IMarketplaceRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.member.MarketplaceMember;
import com.hongsolo.taskspree.modules.marketplace.domain.member.enums.MemberStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.member.repository.IMarketplaceMemberRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.role.MarketplaceRole;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.Permission;
import com.hongsolo.taskspree.modules.marketplace.domain.role.repository.IMarketplaceRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateRoleDisplayNameCommandHandler
        implements CommandHandler<UpdateRoleDisplayNameCommand, Result<Void>> {

    private final IMarketplaceRepository marketplaceRepository;
    private final IMarketplaceMemberRepository marketplaceMemberRepository;
    private final IMarketplaceRoleRepository marketplaceRoleRepository;

    @Override
    @Transactional
    public Result<Void> handle(UpdateRoleDisplayNameCommand command) {
        log.info("Updating role display name: marketplace={}, role={}, user={}",
                command.marketplaceId(), command.roleType(), command.userId());

        // 1. Find marketplace
        Marketplace marketplace = marketplaceRepository.findById(command.marketplaceId())
                .orElse(null);

        if (marketplace == null) {
            log.warn("Marketplace not found: {}", command.marketplaceId());
            return Result.failure(MarketplaceErrors.MARKETPLACE_NOT_FOUND);
        }

        if (!marketplace.isActive()) {
            log.warn("Marketplace is archived: {}", command.marketplaceId());
            return Result.failure(MarketplaceErrors.MARKETPLACE_ARCHIVED);
        }

        // 2. Check requester has permission
        MarketplaceMember requester = marketplaceMemberRepository
                .findByMarketplaceIdAndUserIdAndStatus(command.marketplaceId(), command.userId(), MemberStatus.ACTIVE)
                .orElse(null);

        if (requester == null) {
            log.warn("User {} is not a member of marketplace {}", command.userId(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.NOT_A_MEMBER);
        }

        if (!requester.hasPermission(Permission.MANAGE_MARKETPLACE)) {
            log.warn("User {} lacks MANAGE_MARKETPLACE permission", command.userId());
            return Result.failure(MarketplaceErrors.INSUFFICIENT_PERMISSIONS);
        }

        // 3. Find the role
        MarketplaceRole role = marketplaceRoleRepository
                .findByMarketplaceIdAndRoleType(command.marketplaceId(), command.roleType())
                .orElse(null);

        if (role == null) {
            log.error("Role {} not found in marketplace {}", command.roleType(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.ROLE_NOT_FOUND);
        }

        // 4. Update display name and description
        role.updateDisplayInfo(
                command.displayName(),
                command.description() != null ? command.description() : role.getDescription()
        );
        marketplaceRoleRepository.save(role);

        log.info("Role display name updated: marketplace={}, role={}, newName={}",
                command.marketplaceId(), command.roleType(), command.displayName());

        return Result.success(null);
    }
}
