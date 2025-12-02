package com.hongsolo.taskspree.modules.marketplace.application.marketplace.UpdateMarketplace;

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
public class UpdateMarketplaceCommandHandler
        implements CommandHandler<UpdateMarketplaceCommand, Result<Void>> {

    private final IMarketplaceRepository marketplaceRepository;
    private final IMarketplaceMemberRepository marketplaceMemberRepository;

    @Override
    @Transactional
    public Result<Void> handle(UpdateMarketplaceCommand command) {
        log.info("Updating marketplace: {} by user: {}", command.marketplaceId(), command.userId());

        // 1. Find marketplace
        Marketplace marketplace = marketplaceRepository.findById(command.marketplaceId())
                .orElse(null);

        if (marketplace == null) {
            log.warn("Marketplace not found: {}", command.marketplaceId());
            return Result.failure(MarketplaceErrors.MARKETPLACE_NOT_FOUND);
        }

        if (!marketplace.isActive()) {
            log.warn("Cannot update archived marketplace: {}", command.marketplaceId());
            return Result.failure(MarketplaceErrors.MARKETPLACE_ARCHIVED);
        }

        // 2. Check permissions
        MarketplaceMember member = marketplaceMemberRepository
                .findByMarketplaceIdAndUserIdAndStatus(command.marketplaceId(), command.userId(), MemberStatus.ACTIVE)
                .orElse(null);

        if (member == null) {
            log.warn("User {} is not a member of marketplace {}", command.userId(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.NOT_A_MEMBER);
        }

        if (!member.hasPermission(Permission.MANAGE_MARKETPLACE)) {
            log.warn("User {} lacks MANAGE_MARKETPLACE permission", command.userId());
            return Result.failure(MarketplaceErrors.INSUFFICIENT_PERMISSIONS);
        }

        // 3. Update basic info
        if (command.name() != null || command.description() != null) {
            String newName = command.name() != null ? command.name() : marketplace.getName();
            String newDescription = command.description() != null ? command.description() : marketplace.getDescription();
            marketplace.updateInfo(newName, newDescription);
        }

        // 4. Update settings
        if (command.defaultTaskDurationDays() != null ||
                command.autoCloseSlotsPercentage() != null ||
                command.reservationTimeoutDays() != null) {
            marketplace.updateSettings(
                    command.defaultTaskDurationDays(),
                    command.autoCloseSlotsPercentage(),
                    command.reservationTimeoutDays()
            );
        }

        marketplaceRepository.save(marketplace);

        log.info("Marketplace updated successfully: {}", command.marketplaceId());
        return Result.success(null);
    }
}
