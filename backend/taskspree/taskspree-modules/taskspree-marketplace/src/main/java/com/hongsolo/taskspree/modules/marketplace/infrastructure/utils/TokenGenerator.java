package com.hongsolo.taskspree.modules.marketplace.infrastructure.utils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for generating secure random tokens.
 */
public final class TokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private static final int DEFAULT_TOKEN_BYTES = 32; // 256 bits

    private TokenGenerator() {
        // Prevent instantiation
    }

    /**
     * Generate a secure random token.
     *
     * @return A URL-safe base64 encoded token
     */
    public static String generate() {
        return generate(DEFAULT_TOKEN_BYTES);
    }

    /**
     * Generate a secure random token with specified byte length.
     *
     * @param byteLength Number of random bytes
     * @return A URL-safe base64 encoded token
     */
    public static String generate(int byteLength) {
        byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return ENCODER.encodeToString(bytes);
    }
}