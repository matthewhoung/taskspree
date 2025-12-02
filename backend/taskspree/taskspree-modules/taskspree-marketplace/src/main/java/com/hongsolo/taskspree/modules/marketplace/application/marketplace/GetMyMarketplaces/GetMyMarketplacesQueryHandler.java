package com.hongsolo.taskspree.modules.marketplace.application.marketplace.GetMyMarketplaces;

import com.hongsolo.taskspree.common.application.cqrs.QueryHandler;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.member.MarketplaceMember;
import com.hongsolo.taskspree.modules.marketplace.domain.member.enums.MemberStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.member.repository.IMarketplaceMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetMyMarketplacesQueryHandler
        implements QueryHandler<GetMyMarketplacesQuery, List<MarketplaceSummaryResponse>> {

    private final IMarketplaceMemberRepository marketplaceMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MarketplaceSummaryResponse> handle(GetMyMarketplacesQuery query) {
        log.debug("Fetching marketplaces for user: {}", query.userId());

        List<MarketplaceMember> memberships = marketplaceMemberRepository
                .findByUserIdAndStatus(query.userId(), MemberStatus.ACTIVE);

        return memberships.stream()
                .map(this::toMarketplaceSummaryRsp)
                .toList();
    }

    private MarketplaceSummaryResponse toMarketplaceSummaryRsp(MarketplaceMember member) {
        Marketplace marketplace = member.getMarketplace();

        long memberCount = marketplaceMemberRepository
                .countByMarketplaceIdAndStatus(marketplace.getId(), MemberStatus.ACTIVE);

        return new MarketplaceSummaryResponse(
                marketplace.getId(),
                marketplace.getName(),
                marketplace.getSlug(),
                marketplace.getDescription(),
                marketplace.getLogoFileId(),
                marketplace.getStatus().name(),
                member.getRoleType().name(),
                member.getRole().getDisplayName(),
                member.isOwner(),
                memberCount
        );
    }
}
