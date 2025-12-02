package com.hongsolo.taskspree.modules.marketplace.application.member.AcceptInvite;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.MarketplaceInvite;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.repository.IMarketplaceInviteRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.MarketplaceErrors;
import com.hongsolo.taskspree.modules.marketplace.domain.member.MarketplaceMember;
import com.hongsolo.taskspree.modules.marketplace.domain.member.enums.MemberStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.member.repository.IMarketplaceMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcceptInviteCommandHandler
        implements CommandHandler<AcceptInviteCommand, Result<AcceptInviteResponse>> {

    private final IMarketplaceInviteRepository marketplaceInviteRepository;
    private final IMarketplaceMemberRepository marketplaceMemberRepository;

    @Override
    @Transactional
    public Result<AcceptInviteResponse> handle(AcceptInviteCommand command) {
        log.info("Processing invite acceptance: token={}, user={}", command.token(), command.userId());

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

        // 3. Check invite can be accepted
        if (!invite.canBeAccepted()) {
            if (invite.isExpired()) {
                log.warn("Invite expired: {}", invite.getId());
                invite.markExpired();
                marketplaceInviteRepository.save(invite);
                return Result.failure(MarketplaceErrors.INVITE_EXPIRED);
            }

            switch (invite.getStatus()) {
                case ACCEPTED -> {
                    log.warn("Invite already used: {}", invite.getId());
                    return Result.failure(MarketplaceErrors.INVITE_ALREADY_USED);
                }
                case CANCELLED -> {
                    log.warn("Invite cancelled: {}", invite.getId());
                    return Result.failure(MarketplaceErrors.INVITE_CANCELLED);
                }
                case DECLINED -> {
                    log.warn("Invite was declined: {}", invite.getId());
                    return Result.failure(MarketplaceErrors.INVITE_ALREADY_USED);
                }
                default -> {
                    return Result.failure(MarketplaceErrors.INVITE_NOT_FOUND);
                }
            }
        }

        // 4. Check marketplace is active
        Marketplace marketplace = invite.getMarketplace();
        if (!marketplace.isActive()) {
            log.warn("Marketplace is archived: {}", marketplace.getId());
            return Result.failure(MarketplaceErrors.MARKETPLACE_ARCHIVED);
        }

        // 5. Check user is not already a member
        boolean alreadyMember = marketplaceMemberRepository
                .existsByMarketplaceIdAndUserIdAndStatus(marketplace.getId(), command.userId(), MemberStatus.ACTIVE);

        if (alreadyMember) {
            log.warn("User {} is already a member of marketplace {}", command.userId(), marketplace.getId());
            invite.accept();
            marketplaceInviteRepository.save(invite);
            return Result.failure(MarketplaceErrors.ALREADY_A_MEMBER);
        }

        // 6. Accept invite
        invite.accept();
        marketplaceInviteRepository.save(invite);

        // 7. Create member
        MarketplaceMember member = MarketplaceMember.create(
                marketplace,
                command.userId(),
                invite.getRole()
        );
        member = marketplaceMemberRepository.save(member);

        log.info("User {} joined marketplace {} with role {}",
                command.userId(), marketplace.getId(), invite.getRole().getRoleType());

        return Result.success(AcceptInviteResponse.of(
                member.getId(),
                marketplace.getId(),
                marketplace.getName(),
                marketplace.getSlug(),
                invite.getRole().getRoleType().name(),
                invite.getRole().getDisplayName()
        ));
    }
}
