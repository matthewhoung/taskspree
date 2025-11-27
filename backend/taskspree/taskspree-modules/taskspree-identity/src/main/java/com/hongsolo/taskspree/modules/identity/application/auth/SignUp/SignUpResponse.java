package com.hongsolo.taskspree.modules.identity.application.auth.SignUp;

import java.util.UUID;

public record SignUpResponse(
        UUID identityId,
        String email,
        String message
) {
    public static SignUpResponse of(UUID identityId, String email) {
        return new SignUpResponse(
                identityId,
                email,
                "Account created successfully");
    }
}
