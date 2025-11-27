package com.hongsolo.taskspree.modules.identity.presentation.auth.dto;

public record SignUpRequest(
        String email,
        String password
) {
}