package com.hongsolo.taskspree.modules.marketplace.application.role.GetMarketplaceRoles;

import com.hongsolo.taskspree.common.application.cqrs.QueryHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.MarketplaceErrors;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.repository.IMarketplaceRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.member.enums.MemberStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.member.repository.IMarketplaceMemberRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.role.MarketplaceRole;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.Permission;
import com.hongsolo.taskspree.modules.marketplace.domain.role.repository.IMarketplaceRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetMarketplaceRolesQueryHandler
        implements QueryHandler<GetMarketplaceRolesQuery, Result<List<RoleSummaryResponse>>> {

    private final IMarketplaceRepository marketplaceRepository;
    private final IMarketplaceMemberRepository marketplaceMemberRepository;
    private final IMarketplaceRoleRepository marketplaceRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public Result<List<RoleSummaryResponse>> handle(GetMarketplaceRolesQuery query) {
        log.debug("Fetching roles for marketplace: {} by user: {}", query.marketplaceId(), query.userId());

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

        // 3. Get all roles
        List<MarketplaceRole> roles = marketplaceRoleRepository.findByMarketplaceId(query.marketplaceId());

        List<RoleSummaryResponse> dtos = roles.stream()
                .map(this::toRoleSummaryRsp)
                .toList();

        return Result.success(dtos);
    }

    private RoleSummaryResponse toRoleSummaryRsp(MarketplaceRole role) {
        return new RoleSummaryResponse(
                role.getId(),
                role.getRoleType().name(),
                role.getDisplayName(),
                role.getDescription(),
                role.getPermissions().stream()
                        .map(Permission::name)
                        .toList()
        );
    }
}
