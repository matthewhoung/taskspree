package com.hongsolo.taskspree.modules.marketplace.application.member.TransferOwnership;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.MarketplaceErrors;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.repository.IMarketplaceRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.member.MarketplaceMember;
import com.hongsolo.taskspree.modules.marketplace.domain.member.enums.MemberStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.member.repository.IMarketplaceMemberRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.role.MarketplaceRole;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;
import com.hongsolo.taskspree.modules.marketplace.domain.role.repository.IMarketplaceRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferOwnershipCommandHandler
        implements CommandHandler<TransferOwnershipCommand, Result<Void>> {

    private final IMarketplaceRepository marketplaceRepository;
    private final IMarketplaceMemberRepository marketplaceMemberRepository;
    private final IMarketplaceRoleRepository marketplaceRoleRepository;

    @Override
    @Transactional
    public Result<Void> handle(TransferOwnershipCommand command) {
        log.info("Processing ownership transfer: marketplace={}, from={}, to={}",
                command.marketplaceId(), command.currentOwnerUserId(), command.newOwnerUserId());

        // 1. Cannot transfer to self
        if (command.currentOwnerUserId().equals(command.newOwnerUserId())) {
            log.warn("Attempted to transfer ownership to self");
            return Result.failure(MarketplaceErrors.CANNOT_TRANSFER_TO_SELF);
        }

        // 2. Find marketplace
        Marketplace marketplace = marketplaceRepository.findById(command.marketplaceId())
                .orElse(null);

        if (marketplace == null) {
            log.warn("Marketplace not found: {}", command.marketplaceId());
            return Result.failure(MarketplaceErrors.MARKETPLACE_NOT_FOUND);
        }

        // 3. Verify current owner
        if (!marketplace.isOwner(command.currentOwnerUserId())) {
            log.warn("User {} is not the owner of marketplace {}", command.currentOwnerUserId(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.ACCESS_DENIED);
        }

        // 4. Find new owner as existing member
        MarketplaceMember newOwnerMember = marketplaceMemberRepository
                .findByMarketplaceIdAndUserIdAndStatus(command.marketplaceId(), command.newOwnerUserId(), MemberStatus.ACTIVE)
                .orElse(null);

        if (newOwnerMember == null) {
            log.warn("New owner {} is not a member of marketplace {}", command.newOwnerUserId(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.TRANSFER_TARGET_NOT_MEMBER);
        }

        // 5. Get roles
        MarketplaceRole ownerRole = marketplaceRoleRepository
                .findByMarketplaceIdAndRoleType(command.marketplaceId(), RoleType.OWNER)
                .orElseThrow(() -> new IllegalStateException("OWNER role not found"));

        MarketplaceRole managerRole = marketplaceRoleRepository
                .findByMarketplaceIdAndRoleType(command.marketplaceId(), RoleType.MANAGER)
                .orElseThrow(() -> new IllegalStateException("MANAGER role not found"));

        // 6. Find current owner member
        MarketplaceMember currentOwnerMember = marketplaceMemberRepository
                .findByMarketplaceIdAndUserIdAndStatus(command.marketplaceId(), command.currentOwnerUserId(), MemberStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("Current owner member not found"));

        // 7. Transfer: demote old owner to manager, promote new owner
        currentOwnerMember.changeRole(managerRole);
        newOwnerMember.changeRole(ownerRole);

        marketplaceMemberRepository.save(currentOwnerMember);
        marketplaceMemberRepository.save(newOwnerMember);

        // 8. Update marketplace owner reference
        marketplace.transferOwnership(command.newOwnerUserId());
        marketplaceRepository.save(marketplace);

        log.info("Ownership transferred: marketplace={}, newOwner={}", command.marketplaceId(), command.newOwnerUserId());
        return Result.success(null);
    }
}
