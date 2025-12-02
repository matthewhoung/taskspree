package com.hongsolo.taskspree.modules.marketplace.presentation;

import com.hongsolo.taskspree.common.application.cqrs.ICommandBus;
import com.hongsolo.taskspree.common.application.cqrs.IQueryBus;
import com.hongsolo.taskspree.common.application.services.IUserFacadeService;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.common.presentation.ApiController;
import com.hongsolo.taskspree.modules.marketplace.application.member.CancelInvite.CancelInviteCommand;
import com.hongsolo.taskspree.modules.marketplace.application.member.ChangeMemberRole.ChangeMemberRoleCommand;
import com.hongsolo.taskspree.modules.marketplace.application.member.GetMarketplaceInvites.GetMarketplaceInvitesQuery;
import com.hongsolo.taskspree.modules.marketplace.application.member.GetMarketplaceInvites.InviteSummaryResponse;
import com.hongsolo.taskspree.modules.marketplace.application.member.GetMarketplaceMembers.GetMarketplaceMembersQuery;
import com.hongsolo.taskspree.modules.marketplace.application.member.GetMarketplaceMembers.MemberSummaryResponse;
import com.hongsolo.taskspree.modules.marketplace.application.member.InviteMember.InviteMemberCommand;
import com.hongsolo.taskspree.modules.marketplace.application.member.InviteMember.InviteMemberResponse;
import com.hongsolo.taskspree.modules.marketplace.application.member.LeaveMarketplace.LeaveMarketplaceCommand;
import com.hongsolo.taskspree.modules.marketplace.application.member.RemoveMember.RemoveMemberCommand;
import com.hongsolo.taskspree.modules.marketplace.application.member.TransferOwnership.TransferOwnershipCommand;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;
import com.hongsolo.taskspree.modules.marketplace.presentation.dto.ChangeMemberRoleRequest;
import com.hongsolo.taskspree.modules.marketplace.presentation.dto.InviteMemberRequest;
import com.hongsolo.taskspree.modules.marketplace.presentation.dto.TransferOwnershipRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/marketplaces/{marketplaceId}/members")
@RequiredArgsConstructor
public class MemberController extends ApiController {

    private final ICommandBus commandBus;
    private final IQueryBus queryBus;
    private final IUserFacadeService userFacadeService;

    /**
     * Get all members of a marketplace
     */
    @GetMapping
    public ResponseEntity<?> getMembers(@PathVariable("marketplaceId") UUID marketplaceId) {
        UUID userId = getCurrentUserId();

        GetMarketplaceMembersQuery query = new GetMarketplaceMembersQuery(marketplaceId, userId);
        Result<List<MemberSummaryResponse>> result = queryBus.execute(query);

        return handleResult(result);
    }

    /**
     * Get all invites for the marketplace (for owners/managers)
     */
    @GetMapping("/invites")
    public ResponseEntity<?> getInvites(@PathVariable("marketplaceId") UUID marketplaceId) {
        UUID userId = getCurrentUserId();

        GetMarketplaceInvitesQuery query = new GetMarketplaceInvitesQuery(marketplaceId, userId);
        Result<List<InviteSummaryResponse>> result = queryBus.execute(query);

        return handleResult(result);
    }

    /**
     * Invite a user to the marketplace
     */
    @PostMapping("/invite")
    public ResponseEntity<?> inviteMember(
            @PathVariable("marketplaceId") UUID marketplaceId,
            @RequestBody InviteMemberRequest request
    ) {
        UUID userId = getCurrentUserId();

        RoleType roleType = RoleType.valueOf(request.roleType());

        InviteMemberCommand command = new InviteMemberCommand(
                marketplaceId,
                userId,
                request.inviteeEmail(),
                roleType
        );

        Result<InviteMemberResponse> result = commandBus.execute(command);
        return handleResult(result);
    }

    /**
     * Cancel a pending invite
     */
    @DeleteMapping("/invites/{inviteId}")
    public ResponseEntity<?> cancelInvite(
            @PathVariable("marketplaceId") UUID marketplaceId,
            @PathVariable("inviteId") UUID inviteId
    ) {
        UUID userId = getCurrentUserId();

        CancelInviteCommand command = new CancelInviteCommand(inviteId, userId);
        Result<Void> result = commandBus.execute(command);

        return handleResult(result);
    }

    /**
     * Remove a member from the marketplace
     */
    @DeleteMapping("/{memberUserId}")
    public ResponseEntity<?> removeMember(
            @PathVariable("marketplaceId") UUID marketplaceId,
            @PathVariable("memberUserId") UUID memberUserId
    ) {
        UUID userId = getCurrentUserId();

        RemoveMemberCommand command = new RemoveMemberCommand(marketplaceId, memberUserId, userId);
        Result<Void> result = commandBus.execute(command);

        return handleResult(result);
    }

    /**
     * Change a member's role
     */
    @PatchMapping("/{memberUserId}/role")
    public ResponseEntity<?> changeMemberRole(
            @PathVariable("marketplaceId") UUID marketplaceId,
            @PathVariable("memberUserId") UUID memberUserId,
            @RequestBody ChangeMemberRoleRequest request
    ) {
        UUID userId = getCurrentUserId();

        RoleType roleType = RoleType.valueOf(request.roleType());

        ChangeMemberRoleCommand command = new ChangeMemberRoleCommand(
                marketplaceId,
                memberUserId,
                roleType,
                userId
        );

        Result<Void> result = commandBus.execute(command);
        return handleResult(result);
    }

    /**
     * Transfer marketplace ownership
     */
    @PostMapping("/transfer-ownership")
    public ResponseEntity<?> transferOwnership(
            @PathVariable("marketplaceId") UUID marketplaceId,
            @RequestBody TransferOwnershipRequest request
    ) {
        UUID userId = getCurrentUserId();

        TransferOwnershipCommand command = new TransferOwnershipCommand(
                marketplaceId,
                request.newOwnerUserId(),
                userId
        );

        Result<Void> result = commandBus.execute(command);
        return handleResult(result);
    }

    /**
     * Leave the marketplace
     */
    @PostMapping("/leave")
    public ResponseEntity<?> leaveMarketplace(@PathVariable("marketplaceId") UUID marketplaceId) {
        UUID userId = getCurrentUserId();

        LeaveMarketplaceCommand command = new LeaveMarketplaceCommand(marketplaceId, userId);
        Result<Void> result = commandBus.execute(command);

        return handleResult(result);
    }

    // === Helper Methods ===

    private UUID getCurrentUserId() {
        return userFacadeService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"))
                .userId();
    }
}
