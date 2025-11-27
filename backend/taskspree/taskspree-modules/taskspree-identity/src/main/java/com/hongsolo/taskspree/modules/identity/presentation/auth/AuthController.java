package com.hongsolo.taskspree.modules.identity.presentation.auth;

import com.hongsolo.taskspree.common.application.cqrs.CommandBus;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.common.presentation.ApiController;
import com.hongsolo.taskspree.modules.identity.application.auth.RefreshToken.RefreshTokenCommand;
import com.hongsolo.taskspree.modules.identity.application.auth.SignIn.SignInCommand;
import com.hongsolo.taskspree.modules.identity.application.auth.SignUp.SignUpCommand;
import com.hongsolo.taskspree.modules.identity.application.auth.SignUp.SignUpResponse;
import com.hongsolo.taskspree.modules.identity.application.auth.dto.AuthTokenResponse;
import com.hongsolo.taskspree.modules.identity.presentation.auth.dto.RefreshTokenRequest;
import com.hongsolo.taskspree.modules.identity.presentation.auth.dto.SignInRequest;
import com.hongsolo.taskspree.modules.identity.presentation.auth.dto.SignUpRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends ApiController {

    private final CommandBus commandBus;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest request) {
        SignUpCommand command = new SignUpCommand(
                request.email(),
                request.password()
        );

        Result<SignUpResponse> result = commandBus.execute(command);

        return handleResult(result);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(
            @RequestBody SignInRequest request,
            HttpServletRequest httpRequest
    ) {
        SignInCommand command = new SignInCommand(
                request.email(),
                request.password(),
                extractDeviceInfo(httpRequest),
                extractClientIp(httpRequest)
        );

        Result<AuthTokenResponse> result = commandBus.execute(command);

        return handleResult(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        RefreshTokenCommand command = new RefreshTokenCommand(
                request.refreshToken(),
                extractDeviceInfo(httpRequest),
                extractClientIp(httpRequest)
        );

        Result<AuthTokenResponse> result = commandBus.execute(command);

        return handleResult(result);
    }

    /**
     * Extracts device information from the User-Agent header.
     */
    private String extractDeviceInfo(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    /**
     * Extracts the client IP address, handling proxy headers.
     */
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs; the first one is the client
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}