package com.hongsolo.taskspree.modules.marketplace.application.marketplace.GetMarketplaceBySlug;

import com.hongsolo.taskspree.common.application.cqrs.QueryHandler;
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
public class GetMarketplaceBySlugQueryHandler
        implements QueryHandler<GetMarketplaceBySlugQuery, Result<MarketplaceDetailResponse>> {

    private final IMarketplaceRepository marketplaceRepository;
    private final IMarketplaceMemberRepository marketplaceMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public Result<MarketplaceDetailResponse> handle(GetMarketplaceBySlugQuery query) {
        log.debug("Fetching marketplace by slug: {} for user: {}", query.slug(), query.userId());

        // 1. Find marketplace
        Marketplace marketplace = marketplaceRepository.findBySlug(query.slug())
                .orElse(null);

        if (marketplace == null) {
            log.warn("Marketplace not found: {}", query.slug());
            return Result.failure(MarketplaceErrors.MARKETPLACE_NOT_FOUND);
        }

        // 2. Check membership
        MarketplaceMember member = marketplaceMemberRepository
                .findByMarketplaceIdAndUserIdAndStatus(marketplace.getId(), query.userId(), MemberStatus.ACTIVE)
                .orElse(null);

        if (member == null) {
            log.warn("User {} is not a member of marketplace {}", query.userId(), query.slug());
            return Result.failure(MarketplaceErrors.NOT_A_MEMBER);
        }

        // 3. Build response
        MarketplaceDetailResponse dto = toMarketplaceDetailRsp(marketplace, member);

        return Result.success(dto);
    }

    private MarketplaceDetailResponse toMarketplaceDetailRsp(Marketplace marketplace, MarketplaceMember member) {
        return new MarketplaceDetailResponse(
                marketplace.getId(),
                marketplace.getName(),
                marketplace.getSlug(),
                marketplace.getDescription(),
                marketplace.getLogoFileId(),
                marketplace.getStatus().name(),
                marketplace.getOwnerId(),
                new MarketplaceDetailResponse.SettingsDto(
                        marketplace.getDefaultTaskDurationDays(),
                        marketplace.getAutoCloseSlotsPercentage(),
                        marketplace.getReservationTimeoutDays()
                ),
                new MarketplaceDetailResponse.UserContextDto(
                        member.getId(),
                        member.getRoleType().name(),
                        member.getRole().getDisplayName(),
                        member.getPermissions().stream()
                                .map(Permission::name)
                                .toList(),
                        member.isOwner()
                ),
                marketplace.getCreatedAt(),
                marketplace.getUpdatedAt()
        );
    }
}
