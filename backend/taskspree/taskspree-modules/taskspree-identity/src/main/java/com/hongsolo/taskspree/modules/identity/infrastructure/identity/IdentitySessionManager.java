package com.hongsolo.taskspree.modules.identity.infrastructure.identity;

import com.hongsolo.taskspree.modules.identity.domain.identity.IdentitySession;
import com.hongsolo.taskspree.modules.identity.domain.identity.repository.IIdentitySessionManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdentitySessionManager
        extends JpaRepository<IdentitySession, UUID>, IIdentitySessionManager {

    Optional<IdentitySession> findByRefreshToken(String refreshToken);

    /**
     * Find all active (non-revoked, non-expired) sessions for a user.
     */
    @Query("SELECT s FROM IdentitySession s " +
            "WHERE s.identityUser.id = :identityId " +
            "AND s.revoked = false " +
            "AND s.expiresAt > CURRENT_TIMESTAMP")
    List<IdentitySession> findActiveByIdentityId(@Param("identityId") UUID identityId);

    /**
     * Revoke all active sessions for a user.
     * Returns the number of sessions revoked.
     */
    @Modifying
    @Query("UPDATE IdentitySession s " +
            "SET s.revoked = true, s.revokedReason = :reason, s.revokedAt = :revokedAt " +
            "WHERE s.identityUser.id = :identityId AND s.revoked = false")
    int revokeAllByIdentityId(
            @Param("identityId") UUID identityId,
            @Param("reason") String reason,
            @Param("revokedAt") Instant revokedAt
    );

    /**
     * Delete expired and revoked sessions for cleanup.
     * Returns the number of sessions deleted.
     */
    @Modifying
    @Query("DELETE FROM IdentitySession s " +
            "WHERE s.expiresAt < CURRENT_TIMESTAMP AND s.revoked = true")
    int deleteExpiredAndRevoked();

    // ==================== Default Method Bridges ====================

    @Override
    default List<IdentitySession> findActiveSessionsByIdentityId(UUID identityId) {
        return findActiveByIdentityId(identityId);
    }

    @Override
    default int revokeAllSessionsByIdentityId(UUID identityId, String reason) {
        return revokeAllByIdentityId(identityId, reason, Instant.now());
    }

    @Override
    default int deleteExpiredSessions() {
        return deleteExpiredAndRevoked();
    }
}