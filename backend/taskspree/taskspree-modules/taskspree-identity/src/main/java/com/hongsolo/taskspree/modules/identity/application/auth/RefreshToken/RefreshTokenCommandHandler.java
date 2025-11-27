package com.hongsolo.taskspree.modules.identity.application.auth.RefreshToken;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.identity.application.auth.dto.AuthTokenResponse;
import com.hongsolo.taskspree.modules.identity.application.services.TokenProvider;
import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityErrors;
import com.hongsolo.taskspree.modules.identity.domain.identity.IdentitySession;
import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityUser;
import com.hongsolo.taskspree.modules.identity.domain.identity.repository.IIdentitySessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenCommandHandler implements CommandHandler<RefreshTokenCommand, Result<AuthTokenResponse>> {

    private final IIdentitySessionManager identitySessionManager;
    private final TokenProvider tokenProvider;

    @Override
    @Transactional
    public Result<AuthTokenResponse> handle(RefreshTokenCommand command) {
        log.debug("Processing token refresh");

        // 1. Find session by refresh token
        IdentitySession existingSession = identitySessionManager.findByRefreshToken(command.refreshToken())
                .orElse(null);

        if (existingSession == null) {
            log.warn("Token refresh failed: session not found");
            return Result.failure(IdentityErrors.SESSION_NOT_FOUND);
        }

        // 2. Check if session is revoked
        if (existingSession.isRevoked()) {
            log.warn("Token refresh failed: session already revoked - possible token reuse attack");
            // Security: Revoke all sessions for this user on potential token reuse
            identitySessionManager.revokeAllSessionsByIdentityId(
                    existingSession.getIdentityUser().getId(),
                    "Potential token reuse detected"
            );
            return Result.failure(IdentityErrors.TOKEN_REVOKED);
        }

        // 3. Check if session is expired
        if (existingSession.isExpired()) {
            log.warn("Token refresh failed: session expired");
            return Result.failure(IdentityErrors.TOKEN_EXPIRED);
        }

        // 4. Check if user account is enabled
        IdentityUser identityUser = existingSession.getIdentityUser();
        if (!identityUser.isEnabled()) {
            log.warn("Token refresh failed: account disabled");
            return Result.failure(IdentityErrors.ACCOUNT_DISABLED);
        }

        // 5. Token Rotation: Revoke old session
        existingSession.revoke("Token rotation");
        identitySessionManager.save(existingSession);

        // 6. Generate new tokens
        String newAccessToken = tokenProvider.generateAccessToken(identityUser.getId());
        String newRefreshToken = tokenProvider.generateRefreshToken();

        // 7. Create new session
        Instant expiresAt = Instant.now().plusMillis(tokenProvider.getRefreshTokenExpirationMs());

        IdentitySession newSession = IdentitySession.create(
                identityUser,
                newRefreshToken,
                expiresAt,
                command.deviceInfo() != null ? command.deviceInfo() : existingSession.getDeviceInfo(),
                command.ipAddress() != null ? command.ipAddress() : existingSession.getIpAddress()
        );

        identitySessionManager.save(newSession);

        log.info("Token refresh successful for identity: {}", identityUser.getId());

        return Result.success(AuthTokenResponse.of(
                newAccessToken,
                newRefreshToken,
                tokenProvider.getAccessTokenExpirationMs()
        ));
    }
}
