package com.hongsolo.taskspree.modules.identity.domain.identity;

import com.hongsolo.taskspree.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "identity_sessions", schema = "identity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdentitySession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identity_id", nullable = false)
    private IdentityUser identityUser;

    @Column(name = "refresh_token", nullable = false, unique = true)
    private String refreshToken;

    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "revoked_reason", length = 255)
    private String revokedReason;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private IdentitySession(
            IdentityUser identityUser,
            String refreshToken,
            Instant expiresAt,
            String deviceInfo,
            String ipAddress) {
        this.identityUser = identityUser;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.revoked = false;
        this.createdAt = Instant.now();
    }

    public static IdentitySession create(
            IdentityUser identityUser,
            String refreshToken,
            Instant expiresAt,
            String deviceInfo,
            String ipAddress) {
        return new IdentitySession(
                identityUser,
                refreshToken,
                expiresAt,
                deviceInfo,
                ipAddress);
    }

    public void revoke(String reason) {
        this.revoked = true;
        this.revokedReason = reason;
        this.revokedAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    public boolean isValid() {
        return !this.revoked && !isExpired();
    }
}