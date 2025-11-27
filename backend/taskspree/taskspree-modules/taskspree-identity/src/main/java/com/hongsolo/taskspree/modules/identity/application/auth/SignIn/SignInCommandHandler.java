package com.hongsolo.taskspree.modules.identity.application.auth.SignIn;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.identity.application.auth.dto.AuthTokenResponse;
import com.hongsolo.taskspree.modules.identity.application.services.TokenProvider;
import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityErrors;
import com.hongsolo.taskspree.modules.identity.domain.identity.IdentitySession;
import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityUser;
import com.hongsolo.taskspree.modules.identity.domain.identity.repository.IIdentitySessionManager;
import com.hongsolo.taskspree.modules.identity.domain.identity.repository.IIdentityUserManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignInCommandHandler implements CommandHandler<SignInCommand, Result<AuthTokenResponse>> {

    private final IIdentityUserManager identityUserManager;
    private final IIdentitySessionManager identitySessionManager;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Result<AuthTokenResponse> handle(SignInCommand command) {
        log.info("Processing sign-in for email: {}", command.email());

        // 1. Find user by email
        IdentityUser identityUser = identityUserManager.findByEmail(command.email())
                .orElse(null);

        if (identityUser == null) {
            log.warn("Signin failed: user not found - {}", command.email());
            return Result.failure(IdentityErrors.INVALID_CREDENTIALS);
        }

        // 2. Check if account is enabled
        if (!identityUser.isEnabled()) {
            log.warn("Signin failed: account disabled - {}", command.email());
            return Result.failure(IdentityErrors.ACCOUNT_DISABLED);
        }

        // 3. Verify password
        if (!passwordEncoder.matches(command.password(), identityUser.getPasswordHash())) {
            log.warn("Signin failed: invalid password - {}", command.email());
            return Result.failure(IdentityErrors.INVALID_CREDENTIALS);
        }

        // 4. Generate tokens
        String accessToken = tokenProvider.generateAccessToken(identityUser.getId());
        String refreshToken = tokenProvider.generateRefreshToken();

        // 5. Create session
        Instant expiresAt = Instant.now().plusMillis(tokenProvider.getRefreshTokenExpirationMs());

        IdentitySession session = IdentitySession.create(
                identityUser,
                refreshToken,
                expiresAt,
                command.deviceInfo(),
                command.ipAddress()
        );

        identitySessionManager.save(session);

        log.info("Signin successful for email: {}", command.email());

        return Result.success(AuthTokenResponse.of(
                accessToken,
                refreshToken,
                tokenProvider.getAccessTokenExpirationMs()
        ));
    }
}
