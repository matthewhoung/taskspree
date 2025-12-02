package com.hongsolo.taskspree.modules.marketplace.presentation;

import com.hongsolo.taskspree.common.application.cqrs.ICommandBus;
import com.hongsolo.taskspree.common.application.cqrs.IQueryBus;
import com.hongsolo.taskspree.common.application.services.IUserFacadeService;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.common.presentation.ApiController;
import com.hongsolo.taskspree.modules.marketplace.application.member.AcceptInvite.AcceptInviteCommand;
import com.hongsolo.taskspree.modules.marketplace.application.member.AcceptInvite.AcceptInviteResponse;
import com.hongsolo.taskspree.modules.marketplace.application.member.DeclineInvite.DeclineInviteCommand;
import com.hongsolo.taskspree.modules.marketplace.application.member.GetMyPendingInvites.GetMyPendingInvitesQuery;
import com.hongsolo.taskspree.modules.marketplace.application.member.GetMyPendingInvites.PendingInviteSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class InviteController extends ApiController {

    private final ICommandBus commandBus;
    private final IQueryBus queryBus;
    private final IUserFacadeService userFacadeService;

    /**
     * Get current user's pending invites
     */
    @GetMapping
    public ResponseEntity<List<PendingInviteSummaryResponse>> getMyPendingInvites() {
        UUID userId = getCurrentUserId();

        GetMyPendingInvitesQuery query = new GetMyPendingInvitesQuery(userId);
        List<PendingInviteSummaryResponse> result = queryBus.execute(query);

        return ResponseEntity.ok(result);
    }

    /**
     * Accept by token
     */
    @PostMapping("/{token}/accept")
    public ResponseEntity<?> acceptInvite(@PathVariable("token") String token) {
        UUID userId = getCurrentUserId();

        AcceptInviteCommand command = new AcceptInviteCommand(token, userId);
        Result<AcceptInviteResponse> result = commandBus.execute(command);

        return handleResult(result);
    }

    /**
     * Decline by token
     */
    @PostMapping("/{token}/decline")
    public ResponseEntity<?> declineInvite(@PathVariable("token") String token) {
        UUID userId = getCurrentUserId();

        DeclineInviteCommand command = new DeclineInviteCommand(token, userId);
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
