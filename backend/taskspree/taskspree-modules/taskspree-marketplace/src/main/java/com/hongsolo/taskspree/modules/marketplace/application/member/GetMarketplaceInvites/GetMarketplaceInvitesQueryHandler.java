package com.hongsolo.taskspree.modules.marketplace.application.member.GetMarketplaceInvites;

import com.hongsolo.taskspree.common.application.cqrs.QueryHandler;
import com.hongsolo.taskspree.common.application.services.IUserFacadeService;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetMarketplaceInvitesQueryHandler
        implements QueryHandler<GetMarketplaceInvitesQuery, Result<List<InviteSummaryResponse>>> {

    private final IMarketplaceMemberRepository marketplaceMemberRepository;
    private final IMarketplaceInviteRepository marketplaceInviteRepository;
    private final IUserFacadeService userFacadeService;

    @Override
    @Transactional(readOnly = true)
    public Result<List<InviteSummaryResponse>> handle(GetMarketplaceInvitesQuery query) {
        log.debug("Fetching invites for marketplace: {}", query.marketplaceId());

        // 1. Check requester is a member with MANAGE_MEMBERS permission
        MarketplaceMember requester = marketplaceMemberRepository
                .findByMarketplaceIdAndUserIdAndStatus(query.marketplaceId(), query.userId(), MemberStatus.ACTIVE)
                .orElse(null);

        if (requester == null) {
            log.warn("User {} is not a member of marketplace {}", query.userId(), query.marketplaceId());
            return Result.failure(MarketplaceErrors.NOT_A_MEMBER);
        }

        if (!requester.hasPermission(Permission.MANAGE_MEMBERS)) {
            log.warn("User {} lacks MANAGE_MEMBERS permission", query.userId());
            return Result.failure(MarketplaceErrors.INSUFFICIENT_PERMISSIONS);
        }

        // 2. Get all invites for this marketplace
        List<MarketplaceInvite> invites = marketplaceInviteRepository
                .findByMarketplaceId(query.marketplaceId());

        // 3. Batch lookup user emails (both inviters and invitees)
        Set<UUID> userIds = invites.stream()
                .flatMap(invite -> java.util.stream.Stream.of(
                        invite.getInvitedByUserId(),
                        invite.getInviteeUserId()
                ))
                .collect(Collectors.toSet());

        Map<UUID, String> userEmailMap = userIds.stream()
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> userFacadeService.findById(userId)
                                .map(IUserFacadeService.UserDto::email)
                                .orElse("Unknown")
                ));

        // 4. Map to DTOs
        List<InviteSummaryResponse> dtos = invites.stream()
                .map(invite -> toMarketplaceInviteDto(invite, userEmailMap))
                .toList();

        return Result.success(dtos);
    }

    private InviteSummaryResponse toMarketplaceInviteDto(MarketplaceInvite invite, Map<UUID, String> userEmailMap) {
        // Use stored inviteeEmail if available, otherwise lookup
        String inviteeEmail = invite.getInviteeEmail() != null && !invite.getInviteeEmail().isEmpty()
                ? invite.getInviteeEmail()
                : userEmailMap.getOrDefault(invite.getInviteeUserId(), "Unknown");

        return new InviteSummaryResponse(
                invite.getId(),
                inviteeEmail,
                invite.getRole().getRoleType().name(),
                invite.getRole().getDisplayName(),
                invite.getStatus().name(),
                invite.isExpired(),
                userEmailMap.getOrDefault(invite.getInvitedByUserId(), "Unknown"),
                invite.getExpiresAt(),
                invite.getCreatedAt(),
                invite.getAcceptedAt()
        );
    }
}
