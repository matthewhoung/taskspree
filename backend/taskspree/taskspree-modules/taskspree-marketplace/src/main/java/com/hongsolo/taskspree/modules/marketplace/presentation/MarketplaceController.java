package com.hongsolo.taskspree.modules.marketplace.presentation;

import com.hongsolo.taskspree.common.application.cqrs.ICommandBus;
import com.hongsolo.taskspree.common.application.cqrs.IQueryBus;
import com.hongsolo.taskspree.common.application.services.IUserFacadeService;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.common.presentation.ApiController;
import com.hongsolo.taskspree.modules.marketplace.application.marketplace.ArchiveMarketplace.ArchiveMarketplaceCommand;
import com.hongsolo.taskspree.modules.marketplace.application.marketplace.CreateMarketplace.CreateMarketplaceCommand;
import com.hongsolo.taskspree.modules.marketplace.application.marketplace.CreateMarketplace.CreateMarketplaceResponse;
import com.hongsolo.taskspree.modules.marketplace.application.marketplace.GetMarketplaceBySlug.GetMarketplaceBySlugQuery;
import com.hongsolo.taskspree.modules.marketplace.application.marketplace.GetMarketplaceBySlug.MarketplaceDetailResponse;
import com.hongsolo.taskspree.modules.marketplace.application.marketplace.GetMyMarketplaces.GetMyMarketplacesQuery;
import com.hongsolo.taskspree.modules.marketplace.application.marketplace.GetMyMarketplaces.MarketplaceSummaryResponse;
import com.hongsolo.taskspree.modules.marketplace.application.marketplace.UpdateMarketplace.UpdateMarketplaceCommand;
import com.hongsolo.taskspree.modules.marketplace.presentation.dto.CreateMarketplaceRequest;
import com.hongsolo.taskspree.modules.marketplace.presentation.dto.UpdateMarketplaceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/marketplaces")
@RequiredArgsConstructor
public class MarketplaceController extends ApiController {

    private final ICommandBus commandBus;
    private final IQueryBus queryBus;
    private final IUserFacadeService userFacadeService;

    /**
     * Create a new marketplace
     */
    @PostMapping
    public ResponseEntity<?> createMarketplace(@RequestBody CreateMarketplaceRequest request) {
        UUID userId = getCurrentUserId();

        CreateMarketplaceCommand command = new CreateMarketplaceCommand(
                userId,
                request.name(),
                request.description()
        );

        Result<CreateMarketplaceResponse> result = commandBus.execute(command);
        return handleResult(result);
    }

    /**
     * Get all marketplaces the current user is a member of
     */
    @GetMapping
    public ResponseEntity<List<MarketplaceSummaryResponse>> getMyMarketplaces() {
        UUID userId = getCurrentUserId();

        GetMyMarketplacesQuery query = new GetMyMarketplacesQuery(userId);
        List<MarketplaceSummaryResponse> result = queryBus.execute(query);

        return ResponseEntity.ok(result);
    }

    /**
     * Get marketplace details by slug
     */
    @GetMapping("/{slug}")
    public ResponseEntity<?> getMarketplaceBySlug(@PathVariable("slug") String slug) {
        UUID userId = getCurrentUserId();

        GetMarketplaceBySlugQuery query = new GetMarketplaceBySlugQuery(slug, userId);
        Result<MarketplaceDetailResponse> result = queryBus.execute(query);

        return handleResult(result);
    }

    /**
     * Update marketplace information
     */
    @PatchMapping("/{marketplaceId}")
    public ResponseEntity<?> updateMarketplace(
            @PathVariable("marketplaceId") UUID marketplaceId,
            @RequestBody UpdateMarketplaceRequest request
    ) {
        UUID userId = getCurrentUserId();

        UpdateMarketplaceCommand command = new UpdateMarketplaceCommand(
                marketplaceId,
                userId,
                request.name(),
                request.description(),
                request.defaultTaskDurationDays(),
                request.autoCloseSlotsPercentage(),
                request.reservationTimeoutDays()
        );

        Result<Void> result = commandBus.execute(command);
        return handleResult(result);
    }

    /**
     * Archive a marketplace (owner only)
     */
    @DeleteMapping("/{marketplaceId}")
    public ResponseEntity<?> archiveMarketplace(@PathVariable("marketplaceId") UUID marketplaceId) {
        UUID userId = getCurrentUserId();

        ArchiveMarketplaceCommand command = new ArchiveMarketplaceCommand(marketplaceId, userId);
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
