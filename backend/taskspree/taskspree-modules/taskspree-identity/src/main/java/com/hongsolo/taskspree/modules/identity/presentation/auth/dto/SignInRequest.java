package com.hongsolo.taskspree.modules.identity.presentation.auth.dto;

public record SignInRequest(
        String email,
        String password
) {
}