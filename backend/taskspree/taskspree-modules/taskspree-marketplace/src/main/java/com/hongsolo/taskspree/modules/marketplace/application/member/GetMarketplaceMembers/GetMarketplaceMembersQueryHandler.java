package com.hongsolo.taskspree.modules.marketplace.application.member.GetMarketplaceMembers;

import com.hongsolo.taskspree.common.application.cqrs.QueryHandler;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetMarketplaceMembersQueryHandler
        implements QueryHandler<GetMarketplaceMembersQuery, Result<List<MemberSummaryResponse>>> {

    private final IMarketplaceRepository marketplaceRepository;
    private final IMarketplaceMemberRepository marketplaceMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public Result<List<MemberSummaryResponse>> handle(GetMarketplaceMembersQuery query) {
        log.debug("Fetching members for marketplace: {} by user: {}", query.marketplaceId(), query.userId());

        // 1. Find marketplace
        Marketplace marketplace = marketplaceRepository.findById(query.marketplaceId())
                .orElse(null);

        if (marketplace == null) {
            log.warn("Marketplace not found: {}", query.marketplaceId());
            return Result.failure(MarketplaceErrors.MARKETPLACE_NOT_FOUND);
        }

        // 2. Check requester is a member
        boolean isMember = marketplaceMemberRepository
                .existsByMarketplaceIdAndUserIdAndStatus(query.marketplaceId(), query.userId(), MemberStatus.ACTIVE);

        if (!isMember) {
            log.warn("User {} is not a member of marketplace {}", query.userId(), query.marketplaceId());
            return Result.failure(MarketplaceErrors.NOT_A_MEMBER);
        }

        // 3. Get all active members
        List<MarketplaceMember> members = marketplaceMemberRepository
                .findByMarketplaceIdAndStatus(query.marketplaceId(), MemberStatus.ACTIVE);

        List<MemberSummaryResponse> dtos = members.stream()
                .map(this::toMemberSummaryRsp)
                .toList();

        return Result.success(dtos);
    }

    private MemberSummaryResponse toMemberSummaryRsp(MarketplaceMember member) {
        return new MemberSummaryResponse(
                member.getId(),
                member.getUserId(),
                member.getRoleType().name(),
                member.getRole().getDisplayName(),
                member.isOwner(),
                member.getJoinedAt()
        );
    }
}
