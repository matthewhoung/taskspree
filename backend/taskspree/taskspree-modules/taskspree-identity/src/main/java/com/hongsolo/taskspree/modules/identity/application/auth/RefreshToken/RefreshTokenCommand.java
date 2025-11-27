package com.hongsolo.taskspree.modules.identity.application.auth.RefreshToken;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.identity.application.auth.dto.AuthTokenResponse;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenCommand(
        @NotBlank(message = "Refresh token is required")
        String refreshToken,

        // Optional: device info for new session
        String deviceInfo,

        // Optional: IP address for new session
        String ipAddress
) implements Command<Result<AuthTokenResponse>> {

    // Convenience constructor without device/IP info
    public RefreshTokenCommand(String refreshToken) {
        this(refreshToken, null, null);
    }
}
