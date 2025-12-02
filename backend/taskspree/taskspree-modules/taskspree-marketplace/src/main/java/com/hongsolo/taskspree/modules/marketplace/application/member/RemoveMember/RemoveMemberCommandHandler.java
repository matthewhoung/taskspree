package com.hongsolo.taskspree.modules.marketplace.application.member.RemoveMember;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.MarketplaceErrors;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.repository.IMarketplaceRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.member.MarketplaceMember;
import com.hongsolo.taskspree.modules.marketplace.domain.member.enums.MemberStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.member.repository.IMarketplaceMemberRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.Permission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoveMemberCommandHandler
        implements CommandHandler<RemoveMemberCommand, Result<Void>> {

    private final IMarketplaceRepository marketplaceRepository;
    private final IMarketplaceMemberRepository marketplaceMemberRepository;

    @Override
    @Transactional
    public Result<Void> handle(RemoveMemberCommand command) {
        log.info("Processing member removal: marketplace={}, target={}, requester={}",
                command.marketplaceId(), command.memberUserId(), command.requesterId());

        // 1. Cannot remove self (use LeaveMarketplace instead)
        if (command.memberUserId().equals(command.requesterId())) {
            log.warn("User attempted to remove themselves via RemoveMember");
            return Result.failure(MarketplaceErrors.CANNOT_REMOVE_SELF);
        }

        // 2. Find marketplace
        Marketplace marketplace = marketplaceRepository.findById(command.marketplaceId())
                .orElse(null);

        if (marketplace == null) {
            log.warn("Marketplace not found: {}", command.marketplaceId());
            return Result.failure(MarketplaceErrors.MARKETPLACE_NOT_FOUND);
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

        // 4. Find member to remove
        MarketplaceMember targetMember = marketplaceMemberRepository
                .findByMarketplaceIdAndUserIdAndStatus(command.marketplaceId(), command.memberUserId(), MemberStatus.ACTIVE)
                .orElse(null);

        if (targetMember == null) {
            log.warn("Target member {} not found in marketplace {}", command.memberUserId(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.MEMBER_NOT_FOUND);
        }

        // 5. Cannot remove owner
        if (targetMember.isOwner()) {
            log.warn("Attempted to remove owner of marketplace {}", command.marketplaceId());
            return Result.failure(MarketplaceErrors.CANNOT_REMOVE_OWNER);
        }

        // 6. Remove member
        targetMember.remove();
        marketplaceMemberRepository.save(targetMember);

        log.info("Member removed: user={} from marketplace={}", command.memberUserId(), command.marketplaceId());
        return Result.success(null);
    }
}
