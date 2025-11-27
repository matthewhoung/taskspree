package com.hongsolo.taskspree.modules.identity.domain.identity;

import com.hongsolo.taskspree.common.domain.Error;

public final class IdentityErrors {

    private IdentityErrors() {
        // Prevent instantiation
    }

    // User Errors
    public static final Error EMAIL_ALREADY_EXISTS = new Error(
            "Identity.EmailAlreadyExists",
            "An account with this email address already exists",
            Error.ErrorType.CONFLICT
    );

    public static final Error USER_NOT_FOUND = new Error(
            "Identity.UserNotFound",
            "User not found",
            Error.ErrorType.NOT_FOUND
    );

    public static final Error INVALID_CREDENTIALS = new Error(
            "Identity.InvalidCredentials",
            "Invalid email or password",
            Error.ErrorType.VALIDATION
    );

    public static final Error ACCOUNT_DISABLED = new Error(
            "Identity.AccountDisabled",
            "This account has been disabled",
            Error.ErrorType.VALIDATION
    );

    // Token Errors
    public static final Error TOKEN_EXPIRED = new Error(
            "Identity.TokenExpired",
            "The token has expired",
            Error.ErrorType.VALIDATION
    );

    public static final Error TOKEN_INVALID = new Error(
            "Identity.TokenInvalid",
            "The token is invalid",
            Error.ErrorType.VALIDATION
    );

    public static final Error TOKEN_REVOKED = new Error(
            "Identity.TokenRevoked",
            "The token has been revoked",
            Error.ErrorType.VALIDATION
    );

    public static final Error SESSION_NOT_FOUND = new Error(
            "Identity.SessionNotFound",
            "Session not found",
            Error.ErrorType.NOT_FOUND
    );

    // Password Errors
    public static final Error PASSWORD_INVALID_FORMAT = new Error(
            "Identity.PasswordInvalidFormat",
            "Password must contain at least one uppercase letter and one special character",
            Error.ErrorType.VALIDATION
    );

    // Role Errors
    public static final Error ROLE_NOT_FOUND = new Error(
            "Identity.RoleNotFound",
            "Role not found",
            Error.ErrorType.NOT_FOUND
    );
}