package com.hongsolo.taskspree.modules.marketplace.application.member.GetMyPendingInvites;

import com.hongsolo.taskspree.common.application.cqrs.QueryHandler;
import com.hongsolo.taskspree.common.application.services.IUserFacadeService;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.MarketplaceInvite;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.enums.InviteStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.repository.IMarketplaceInviteRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
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
public class GetMyPendingInvitesQueryHandler
        implements QueryHandler<GetMyPendingInvitesQuery, List<PendingInviteSummaryResponse>> {

    private final IMarketplaceInviteRepository marketplaceInviteRepository;
    private final IUserFacadeService userFacadeService;

    @Override
    @Transactional(readOnly = true)
    public List<PendingInviteSummaryResponse> handle(GetMyPendingInvitesQuery query) {
        log.debug("Fetching pending invites for user: {}", query.userId());

        List<MarketplaceInvite> invites = marketplaceInviteRepository
                .findByInviteeUserIdAndStatus(query.userId(), InviteStatus.PENDING);

        // Filter out expired invites
        List<MarketplaceInvite> validInvites = invites.stream()
                .filter(invite -> !invite.isExpired())
                .toList();

        // Batch lookup inviter emails
        Set<UUID> inviterUserIds = validInvites.stream()
                .map(MarketplaceInvite::getInvitedByUserId)
                .collect(Collectors.toSet());

        Map<UUID, String> inviterEmailMap = inviterUserIds.stream()
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> userFacadeService.findById(userId)
                                .map(IUserFacadeService.UserDto::email)
                                .orElse("Unknown")
                ));

        return validInvites.stream()
                .map(invite -> toPendingInviteDto(invite, inviterEmailMap.get(invite.getInvitedByUserId())))
                .toList();
    }

    private PendingInviteSummaryResponse toPendingInviteDto(MarketplaceInvite invite, String inviterEmail) {
        Marketplace marketplace = invite.getMarketplace();

        return new PendingInviteSummaryResponse(
                invite.getId(),
                invite.getToken(),
                marketplace.getId(),
                marketplace.getName(),
                marketplace.getSlug(),
                invite.getRole().getRoleType().name(),
                invite.getRole().getDisplayName(),
                inviterEmail,
                invite.getExpiresAt(),
                invite.getCreatedAt()
        );
    }
}
