package com.hongsolo.taskspree.modules.identity.domain.identity.repository;

import com.hongsolo.taskspree.modules.identity.domain.identity.IdentitySession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IIdentitySessionManager {

    IdentitySession save(IdentitySession session);

    Optional<IdentitySession> findById(UUID id);

    Optional<IdentitySession> findByRefreshToken(String refreshToken);

    List<IdentitySession> findActiveSessionsByIdentityId(UUID identityId);

    int revokeAllSessionsByIdentityId(UUID identityId, String reason);

    int deleteExpiredSessions();
}