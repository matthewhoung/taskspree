package com.hongsolo.taskspree.modules.marketplace.application.role.GetMarketplaceRoles;

import java.util.List;
import java.util.UUID;

public record RoleSummaryResponse(
        UUID roleId,
        String roleType,
        String displayName,
        String description,
        List<String> permissions
) {
}
