package com.hongsolo.taskspree.modules.marketplace.presentation.dto;

import java.util.UUID;

public record TransferOwnershipRequest(
        UUID newOwnerUserId
) {
}
