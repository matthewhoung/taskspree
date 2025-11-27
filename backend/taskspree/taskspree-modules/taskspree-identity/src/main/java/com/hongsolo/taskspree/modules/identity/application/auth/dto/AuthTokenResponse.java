package com.hongsolo.taskspree.modules.identity.application.auth.dto;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType
) {
    public static AuthTokenResponse of(String accessToken, String refreshToken, long expiresInMs) {
        return new AuthTokenResponse(
                accessToken,
                refreshToken,
                expiresInMs / 1000, // Convert to seconds for client
                "Bearer"
        );
    }
}
