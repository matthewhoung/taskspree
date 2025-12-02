package com.hongsolo.taskspree.modules.marketplace.application.member.ChangeMemberRole;

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
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;
import com.hongsolo.taskspree.modules.marketplace.domain.role.repository.IMarketplaceRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeMemberRoleCommandHandler
        implements CommandHandler<ChangeMemberRoleCommand, Result<Void>> {

    private final IMarketplaceRepository marketplaceRepository;
    private final IMarketplaceMemberRepository marketplaceMemberRepository;
    private final IMarketplaceRoleRepository marketplaceRoleRepository;

    @Override
    @Transactional
    public Result<Void> handle(ChangeMemberRoleCommand command) {
        log.info("Processing role change: marketplace={}, member={}, newRole={}, requester={}",
                command.marketplaceId(), command.memberUserId(), command.newRoleType(), command.requesterId());

        // 1. Cannot assign OWNER role (use TransferOwnership instead)
        if (command.newRoleType() == RoleType.OWNER) {
            log.warn("Attempted to assign OWNER role via ChangeMemberRole");
            return Result.failure(MarketplaceErrors.CANNOT_ASSIGN_OWNER_ROLE);
        }

        // 2. Find marketplace
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

        // 3. Check requester has permission
        MarketplaceMember requester = marketplaceMemberRepository
                .findByMarketplaceIdAndUserIdAndStatus(command.marketplaceId(), command.requesterId(), MemberStatus.ACTIVE)
                .orElse(null);

        if (requester == null) {
            log.warn("Requester {} is not a member of marketplace {}", command.requesterId(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.NOT_A_MEMBER);
        }

        if (!requester.hasPermission(Permission.MANAGE_MEMBERS)) {
            log.warn("Requester {} lacks MANAGE_MEMBERS permission", command.requesterId());
            return Result.failure(MarketplaceErrors.INSUFFICIENT_PERMISSIONS);
        }

        // 4. Find member to change
        MarketplaceMember targetMember = marketplaceMemberRepository
                .findByMarketplaceIdAndUserIdAndStatus(command.marketplaceId(), command.memberUserId(), MemberStatus.ACTIVE)
                .orElse(null);

        if (targetMember == null) {
            log.warn("Target member {} not found in marketplace {}", command.memberUserId(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.MEMBER_NOT_FOUND);
        }

        // 5. Cannot change owner's role
        if (targetMember.isOwner()) {
            log.warn("Attempted to change owner's role in marketplace {}", command.marketplaceId());
            return Result.failure(MarketplaceErrors.CANNOT_CHANGE_OWNER_ROLE);
        }

        // 6. Get new role
        MarketplaceRole newRole = marketplaceRoleRepository
                .findByMarketplaceIdAndRoleType(command.marketplaceId(), command.newRoleType())
                .orElse(null);

        if (newRole == null) {
            log.error("Role {} not found in marketplace {}", command.newRoleType(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.ROLE_NOT_FOUND);
        }

        // 7. Check if already has this role
        if (targetMember.getRoleType() == command.newRoleType()) {
            log.debug("Member already has role {}", command.newRoleType());
            return Result.success(null);
        }

        // 8. Change role
        targetMember.changeRole(newRole);
        marketplaceMemberRepository.save(targetMember);

        log.info("Member role changed: user={}, marketplace={}, newRole={}",
                command.memberUserId(), command.marketplaceId(), command.newRoleType());
        return Result.success(null);
    }
}
