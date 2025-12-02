package com.hongsolo.taskspree.modules.marketplace.application.member.LeaveMarketplace;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.MarketplaceErrors;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.repository.IMarketplaceRepository;
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
public class LeaveMarketplaceCommandHandler
        implements CommandHandler<LeaveMarketplaceCommand, Result<Void>> {

    private final IMarketplaceRepository marketplaceRepository;
    private final IMarketplaceMemberRepository marketplaceMemberRepository;

    @Override
    @Transactional
    public Result<Void> handle(LeaveMarketplaceCommand command) {
        log.info("Processing leave request: marketplace={}, user={}", command.marketplaceId(), command.userId());

        // 1. Find marketplace
        Marketplace marketplace = marketplaceRepository.findById(command.marketplaceId())
                .orElse(null);

        if (marketplace == null) {
            log.warn("Marketplace not found: {}", command.marketplaceId());
            return Result.failure(MarketplaceErrors.MARKETPLACE_NOT_FOUND);
        }

        // 2. Find member
        MarketplaceMember member = marketplaceMemberRepository
                .findByMarketplaceIdAndUserIdAndStatus(command.marketplaceId(), command.userId(), MemberStatus.ACTIVE)
                .orElse(null);

        if (member == null) {
            log.warn("User {} is not a member of marketplace {}", command.userId(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.NOT_A_MEMBER);
        }

        // 3. Owner cannot leave (must transfer ownership first)
        if (member.isOwner()) {
            log.warn("Owner {} attempted to leave marketplace {}", command.userId(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.OWNER_CANNOT_LEAVE);
        }

        // 4. Leave the marketplace
        member.remove();
        marketplaceMemberRepository.save(member);

        log.info("User {} left marketplace {}", command.userId(), command.marketplaceId());
        return Result.success(null);
    }
}
