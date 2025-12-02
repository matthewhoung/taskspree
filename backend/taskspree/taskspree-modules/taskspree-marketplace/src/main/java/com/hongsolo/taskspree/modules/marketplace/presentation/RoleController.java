package com.hongsolo.taskspree.modules.marketplace.presentation;

import com.hongsolo.taskspree.common.application.cqrs.ICommandBus;
import com.hongsolo.taskspree.common.application.cqrs.IQueryBus;
import com.hongsolo.taskspree.common.application.services.IUserFacadeService;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.common.presentation.ApiController;
import com.hongsolo.taskspree.modules.marketplace.application.role.GetMarketplaceRoles.GetMarketplaceRolesQuery;
import com.hongsolo.taskspree.modules.marketplace.application.role.GetMarketplaceRoles.RoleSummaryResponse;
import com.hongsolo.taskspree.modules.marketplace.application.role.UpdateRoleDisplayName.UpdateRoleDisplayNameCommand;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;
import com.hongsolo.taskspree.modules.marketplace.presentation.dto.UpdateRoleRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/marketplaces/{marketplaceId}/roles")
@RequiredArgsConstructor
public class RoleController extends ApiController {

    private final ICommandBus commandBus;
    private final IQueryBus queryBus;
    private final IUserFacadeService userFacadeService;

    /**
     * Get all roles in a marketplace
     */
    @GetMapping
    public ResponseEntity<?> getRoles(@PathVariable("marketplaceId") UUID marketplaceId) {
        UUID userId = getCurrentUserId();

        GetMarketplaceRolesQuery query = new GetMarketplaceRolesQuery(marketplaceId, userId);
        Result<List<RoleSummaryResponse>> result = queryBus.execute(query);

        return handleResult(result);
    }

    /**
     * Update a role's display name and description
     */
    @PatchMapping("/{roleType}")
    public ResponseEntity<?> updateRole(
            @PathVariable("marketplaceId") UUID marketplaceId,
            @PathVariable("roleType") String roleType,
            @RequestBody UpdateRoleRequest request
    ) {
        UUID userId = getCurrentUserId();

        RoleType type = RoleType.valueOf(roleType.toUpperCase());

        UpdateRoleDisplayNameCommand command = new UpdateRoleDisplayNameCommand(
                marketplaceId,
                type,
                request.displayName(),
                request.description(),
                userId
        );

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
