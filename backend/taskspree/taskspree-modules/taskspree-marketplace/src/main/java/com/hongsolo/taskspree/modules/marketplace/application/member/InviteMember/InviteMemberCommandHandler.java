package com.hongsolo.taskspree.modules.marketplace.application.member.InviteMember;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.application.services.IUserFacadeService;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.MarketplaceInvite;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.enums.InviteStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.repository.IMarketplaceInviteRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.MarketplaceErrors;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.repository.IMarketplaceRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.member.MarketplaceMember;
import com.hongsolo.taskspree.modules.marketplace.domain.member.enums.MemberStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.member.repository.IMarketplaceMemberRepository;
import com.hongsolo.taskspree.modules.marketplace.domain.role.MarketplaceRole;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.Permission;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;
import com.hongsolo.taskspree.modules.marketplace.domain.role.repository.IMarketplaceRoleRepository;
import com.hongsolo.taskspree.modules.marketplace.infrastructure.utils.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InviteMemberCommandHandler
        implements CommandHandler<InviteMemberCommand, Result<InviteMemberResponse>> {

    private final IMarketplaceRepository marketplaceRepository;
    private final IMarketplaceMemberRepository marketplaceMemberRepository;
    private final IMarketplaceRoleRepository marketplaceRoleRepository;
    private final IMarketplaceInviteRepository marketplaceInviteRepository;
    private final IUserFacadeService userFacadeService;

    @Override
    @Transactional
    public Result<InviteMemberResponse> handle(InviteMemberCommand command) {
        log.info("Processing invite: marketplace={}, inviter={}, inviteeEmail={}, role={}",
                command.marketplaceId(), command.inviterUserId(), command.inviteeEmail(), command.roleType());

        // 1. Lookup invitee by email
        IUserFacadeService.UserDto invitee = userFacadeService.findByEmail(command.inviteeEmail())
                .orElse(null);

        if (invitee == null) {
            log.warn("Invitee not found with email: {}", command.inviteeEmail());
            return Result.failure(MarketplaceErrors.INVITEE_NOT_FOUND);
        }

        // 2. Cannot invite self
        if (command.inviterUserId().equals(invitee.userId())) {
            log.warn("User attempted to invite themselves");
            return Result.failure(MarketplaceErrors.CANNOT_INVITE_SELF);
        }

        // 3. Cannot assign OWNER role via invite
        if (command.roleType() == RoleType.OWNER) {
            log.warn("Attempted to invite with OWNER role");
            return Result.failure(MarketplaceErrors.CANNOT_ASSIGN_OWNER_ROLE);
        }

        // 4. Find marketplace
        Marketplace marketplace = marketplaceRepository.findById(command.marketplaceId())
                .orElse(null);

        if (marketplace == null) {
            log.warn("Marketplace not found: {}", command.marketplaceId());
            return Result.failure(MarketplaceErrors.MARKETPLACE_NOT_FOUND);
        }

        if (!marketplace.isActive()) {
            log.warn("Cannot invite to archived marketplace: {}", command.marketplaceId());
            return Result.failure(MarketplaceErrors.MARKETPLACE_ARCHIVED);
        }

        // 5. Check inviter has permission
        MarketplaceMember inviter = marketplaceMemberRepository
                .findByMarketplaceIdAndUserIdAndStatus(command.marketplaceId(), command.inviterUserId(), MemberStatus.ACTIVE)
                .orElse(null);

        if (inviter == null) {
            log.warn("Inviter {} is not a member of marketplace {}", command.inviterUserId(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.NOT_A_MEMBER);
        }

        if (!inviter.hasPermission(Permission.MANAGE_MEMBERS)) {
            log.warn("Inviter {} lacks MANAGE_MEMBERS permission", command.inviterUserId());
            return Result.failure(MarketplaceErrors.INSUFFICIENT_PERMISSIONS);
        }

        // 6. Check invitee is not already a member
        boolean alreadyMember = marketplaceMemberRepository
                .existsByMarketplaceIdAndUserIdAndStatus(command.marketplaceId(), invitee.userId(), MemberStatus.ACTIVE);

        if (alreadyMember) {
            log.warn("User {} is already a member of marketplace {}", invitee.userId(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.ALREADY_A_MEMBER);
        }

        // 7. Check no pending invite exists
        boolean pendingInviteExists = marketplaceInviteRepository
                .existsByMarketplaceIdAndInviteeUserIdAndStatus(
                        command.marketplaceId(),
                        invitee.userId(),
                        InviteStatus.PENDING
                );

        if (pendingInviteExists) {
            log.warn("Pending invite already exists for user {} in marketplace {}",
                    invitee.userId(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.PENDING_INVITE_EXISTS);
        }

        // 8. Get the role
        MarketplaceRole role = marketplaceRoleRepository
                .findByMarketplaceIdAndRoleType(command.marketplaceId(), command.roleType())
                .orElse(null);

        if (role == null) {
            log.error("Role {} not found in marketplace {}", command.roleType(), command.marketplaceId());
            return Result.failure(MarketplaceErrors.ROLE_NOT_FOUND);
        }

        // 9. Create invite
        String token = TokenGenerator.generate();
        MarketplaceInvite invite = MarketplaceInvite.create(
                marketplace,
                invitee.userId(),
                command.inviteeEmail(),
                role,
                command.inviterUserId(),
                token
        );

        invite = marketplaceInviteRepository.save(invite);

        log.info("Invite created: id={}, inviteeEmail={}", invite.getId(), command.inviteeEmail());

        return Result.success(InviteMemberResponse.of(
                invite.getId(),
                marketplace.getId(),
                command.inviteeEmail(),
                command.roleType().name(),
                role.getDisplayName(),
                invite.getExpiresAt()
        ));
    }
}
