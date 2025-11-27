package com.hongsolo.taskspree.modules.identity.application.services;

import java.util.UUID;

public interface TokenProvider {

    /**
     * Generates an access token for the given identity ID.
     * @param identityId The identity ID to encode in the token
     * @return The generated JWT access token
     */
    String generateAccessToken(UUID identityId);

    /**
     * Generates a refresh token.
     * @return The generated refresh token (UUID-based)
     */
    String generateRefreshToken();

    /**
     * Extracts the identity ID (subject) from a valid access token.
     * @param token The JWT access token
     * @return The identity ID or null if invalid
     */
    UUID extractIdentityId(String token);

    /**
     * Validates the access token.
     * @param token The JWT access token to validate
     * @return true if valid, false otherwise
     */
    boolean validateAccessToken(String token);

    /**
     * Gets the access token expiration time in milliseconds.
     * @return Expiration time in milliseconds
     */
    long getAccessTokenExpirationMs();

    /**
     * Gets the refresh token expiration time in milliseconds.
     * @return Expiration time in milliseconds
     */
    long getRefreshTokenExpirationMs();
}