package com.hongsolo.taskspree.modules.marketplace.application.marketplace.ArchiveMarketplace;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.MarketplaceErrors;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.repository.IMarketplaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveMarketplaceCommandHandler
        implements CommandHandler<ArchiveMarketplaceCommand, Result<Void>> {

    private final IMarketplaceRepository marketplaceRepository;

    @Override
    @Transactional
    public Result<Void> handle(ArchiveMarketplaceCommand command) {
        log.info("Archiving marketplace: {} by user: {}", command.marketplaceId(), command.userId());

        // 1. Find marketplace
        Marketplace marketplace = marketplaceRepository.findById(command.marketplaceId())
                .orElse(null);

        if (marketplace == null) {
            log.warn("Marketplace not found: {}", command.marketplaceId());
            return Result.failure(MarketplaceErrors.MARKETPLACE_NOT_FOUND);
        }

        // 2. Check if user is the owner
        if (!marketplace.isOwner(command.userId())) {
            log.warn("User {} is not the owner of marketplace {}", command.userId(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.ACCESS_DENIED);
        }

        // 3. Check if already archived
        if (!marketplace.isActive()) {
            log.debug("Marketplace already archived: {}", command.marketplaceId());
            return Result.success(null);
        }

        // 4. Archive the marketplace
        marketplace.archive();
        marketplaceRepository.save(marketplace);

        log.info("Marketplace archived successfully: {}", command.marketplaceId());
        return Result.success(null);
    }
}
