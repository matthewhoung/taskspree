package com.hongsolo.taskspree.modules.marketplace.application.member.DeclineInvite;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.MarketplaceInvite;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.repository.IMarketplaceInviteRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.MarketplaceErrors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeclineInviteCommandHandler
        implements CommandHandler<DeclineInviteCommand, Result<Void>> {

    private final IMarketplaceInviteRepository marketplaceInviteRepository;

    @Override
    @Transactional
    public Result<Void> handle(DeclineInviteCommand command) {
        log.info("Processing invite decline: token={}, user={}", command.token(), command.userId());

        // 1. Find invite by token
        MarketplaceInvite invite = marketplaceInviteRepository.findByToken(command.token())
                .orElse(null);

        if (invite == null) {
            log.warn("Invite not found for token: {}", command.token());
            return Result.failure(MarketplaceErrors.INVITE_NOT_FOUND);
        }

        // 2. Check if user is the intended recipient
        if (!invite.getInviteeUserId().equals(command.userId())) {
            log.warn("User {} is not the recipient of invite {}", command.userId(), invite.getId());
            return Result.failure(MarketplaceErrors.NOT_INVITE_RECIPIENT);
        }

        // 3. Check invite is still pending
        if (!invite.isPending()) {
            log.debug("Invite {} is not pending, current status: {}", invite.getId(), invite.getStatus());
            return Result.success(null); // Idempotent - already handled
        }

        // 4. Decline the invite
        invite.decline();
        marketplaceInviteRepository.save(invite);

        log.info("Invite declined: {}", invite.getId());
        return Result.success(null);
    }
}
