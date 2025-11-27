package com.hongsolo.taskspree.modules.identity.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Utility class for accessing security context information.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Prevent instantiation
    }

    /**
     * Gets the current authenticated identity ID from the SecurityContext.
     *
     * @return Optional containing the identity ID if authenticated, empty otherwise
     */
    public static Optional<UUID> getCurrentIdentityId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UUID) {
            return Optional.of((UUID) principal);
        }

        return Optional.empty();
    }

    /**
     * Gets the current authenticated identity ID, throwing if not authenticated.
     *
     * @return The identity ID
     * @throws IllegalStateException if not authenticated
     */
    public static UUID requireCurrentIdentityId() {
        return getCurrentIdentityId()
                .orElseThrow(() -> new IllegalStateException("No authenticated identity found"));
    }

    /**
     * Checks if there is an authenticated identity in the current context.
     *
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UUID;
    }
}