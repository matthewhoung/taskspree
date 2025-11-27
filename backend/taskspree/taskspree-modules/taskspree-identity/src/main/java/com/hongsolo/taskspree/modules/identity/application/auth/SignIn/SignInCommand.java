package com.hongsolo.taskspree.modules.identity.application.auth.SignIn;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.identity.application.auth.dto.AuthTokenResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignInCommand(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        String password,

        // Optional: device info for session tracking
        String deviceInfo,

        // Optional: IP address for session tracking
        String ipAddress
) implements Command<Result<AuthTokenResponse>> {

    // Convenience constructor without device/IP info
    public SignInCommand(String email, String password) {
        this(email, password, null, null);
    }
}
