package com.hongsolo.taskspree.modules.marketplace.application.member.CancelInvite;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.MarketplaceInvite;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.repository.IMarketplaceInviteRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.MarketplaceErrors;
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
public class CancelInviteCommandHandler
        implements CommandHandler<CancelInviteCommand, Result<Void>> {

    private final IMarketplaceInviteRepository marketplaceInviteRepository;
    private final IMarketplaceMemberRepository marketplaceMemberRepository;

    @Override
    @Transactional
    public Result<Void> handle(CancelInviteCommand command) {
        log.info("Processing invite cancellation: inviteId={}, user={}", command.inviteId(), command.userId());

        // 1. Find invite
        MarketplaceInvite invite = marketplaceInviteRepository.findById(command.inviteId())
                .orElse(null);

        if (invite == null) {
            log.warn("Invite not found: {}", command.inviteId());
            return Result.failure(MarketplaceErrors.INVITE_NOT_FOUND);
        }

        // 2. Check if user has permission to cancel
        MarketplaceMember member = marketplaceMemberRepository
                .findByMarketplaceIdAndUserIdAndStatus(
                        invite.getMarketplace().getId(),
                        command.userId(),
                        MemberStatus.ACTIVE
                )
                .orElse(null);

        if (member == null) {
            log.warn("User {} is not a member of marketplace {}",
                    command.userId(), invite.getMarketplace().getId());
            return Result.failure(MarketplaceErrors.NOT_A_MEMBER);
        }

        if (!member.hasPermission(Permission.MANAGE_MEMBERS)) {
            log.warn("User {} lacks MANAGE_MEMBERS permission", command.userId());
            return Result.failure(MarketplaceErrors.INSUFFICIENT_PERMISSIONS);
        }

        // 3. Check invite can be cancelled
        if (!invite.canBeCancelled()) {
            log.debug("Invite {} cannot be cancelled, status: {}", invite.getId(), invite.getStatus());
            return Result.success(null); // Idempotent
        }

        // 4. Cancel the invite
        invite.cancel();
        marketplaceInviteRepository.save(invite);

        log.info("Invite cancelled: {}", invite.getId());
        return Result.success(null);
    }
}
