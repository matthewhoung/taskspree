package com.hongsolo.taskspree.modules.identity.application.auth.SignUp;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.identity.application.auth.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpCommand(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @ValidPassword
        String password
) implements Command<Result<SignUpResponse>> {
}
