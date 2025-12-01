package com.hongsolo.taskspree.modules.marketplace.domain.invite;

import com.hongsolo.taskspree.common.domain.BaseEntity;
import com.hongsolo.taskspree.modules.marketplace.domain.invite.enums.InviteStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.role.MarketplaceRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "marketplace_invites", schema = "marketplace")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketplaceInvite extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marketplace_id", nullable = false)
    private Marketplace marketplace;

    @Column(name = "invitee_user_id", nullable = false)
    private UUID inviteeUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private MarketplaceRole role;

    @Column(name = "invited_by_user_id", nullable = false)
    private UUID invitedByUserId;

    @Column(name = "token", nullable = false, unique = true, length = 100)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InviteStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    private static final int DEFAULT_EXPIRY_DAYS = 7;

    private MarketplaceInvite(
            Marketplace marketplace,
            UUID inviteeUserId,
            MarketplaceRole role,
            UUID invitedByUserId,
            String token
    ) {
        this.marketplace = marketplace;
        this.inviteeUserId = inviteeUserId;
        this.role = role;
        this.invitedByUserId = invitedByUserId;
        this.token = token;
        this.status = InviteStatus.PENDING;
        this.createdAt = Instant.now();
        this.expiresAt = Instant.now().plusSeconds(DEFAULT_EXPIRY_DAYS * 24 * 60 * 60);
    }

    /**
     * Factory method to create a new invite
     */
    public static MarketplaceInvite create(
            Marketplace marketplace,
            UUID inviteeUserId,
            MarketplaceRole role,
            UUID invitedByUserId,
            String token
    ) {
        return new MarketplaceInvite(marketplace, inviteeUserId, role, invitedByUserId, token);
    }

    /**
     * Factory method to create invite with custom expiry
     */
    public static MarketplaceInvite create(
            Marketplace marketplace,
            UUID inviteeUserId,
            MarketplaceRole role,
            UUID invitedByUserId,
            String token,
            int expiryDays
    ) {
        MarketplaceInvite invite = new MarketplaceInvite(marketplace, inviteeUserId, role, invitedByUserId, token);
        invite.expiresAt = Instant.now().plusSeconds(expiryDays * 24L * 60 * 60);
        return invite;
    }

    /**
     * Accept the invite
     */
    public void accept() {
        this.status = InviteStatus.ACCEPTED;
        this.acceptedAt = Instant.now();
    }

    /**
     * Decline the invite
     */
    public void decline() {
        this.status = InviteStatus.DECLINED;
    }

    /**
     * Cancel the invite (by inviter)
     */
    public void cancel() {
        this.status = InviteStatus.CANCELLED;
    }

    /**
     * Mark as expired
     */
    public void markExpired() {
        this.status = InviteStatus.EXPIRED;
    }

    /**
     * Check if invite is pending
     */
    public boolean isPending() {
        return this.status == InviteStatus.PENDING;
    }

    /**
     * Check if invite has expired
     */
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiresAt);
    }

    /**
     * Check if invite is valid (pending and not expired)
     */
    public boolean isValid() {
        return isPending() && !isExpired();
    }

    /**
     * Check if invite can be accepted
     */
    public boolean canBeAccepted() {
        return isValid();
    }

    /**
     * Check if invite can be cancelled
     */
    public boolean canBeCancelled() {
        return isPending();
    }
}