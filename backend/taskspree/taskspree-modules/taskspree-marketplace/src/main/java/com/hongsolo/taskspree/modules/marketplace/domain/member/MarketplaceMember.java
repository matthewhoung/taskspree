package com.hongsolo.taskspree.modules.marketplace.domain.member;

import com.hongsolo.taskspree.common.domain.BaseEntity;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.member.enums.MemberStatus;
import com.hongsolo.taskspree.modules.marketplace.domain.role.MarketplaceRole;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.Permission;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "marketplace_members", schema = "marketplace")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketplaceMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marketplace_id", nullable = false)
    private Marketplace marketplace;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private MarketplaceRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    private MarketplaceMember(Marketplace marketplace, UUID userId, MarketplaceRole role) {
        this.marketplace = marketplace;
        this.userId = userId;
        this.role = role;
        this.status = MemberStatus.ACTIVE;
        this.joinedAt = Instant.now();
    }

    /**
     * Factory method to create a new member
     */
    public static MarketplaceMember create(Marketplace marketplace, UUID userId, MarketplaceRole role) {
        return new MarketplaceMember(marketplace, userId, role);
    }

    /**
     * Change member's role
     */
    public void changeRole(MarketplaceRole newRole) {
        this.role = newRole;
    }

    /**
     * Remove member (soft delete)
     */
    public void remove() {
        this.status = MemberStatus.REMOVED;
    }

    /**
     * Reactivate removed member
     */
    public void reactivate(MarketplaceRole role) {
        this.status = MemberStatus.ACTIVE;
        this.role = role;
    }

    /**
     * Check if member is active
     */
    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }

    /**
     * Check if member is the owner
     */
    public boolean isOwner() {
        return this.role.isOwnerRole();
    }

    /**
     * Check if member is a manager
     */
    public boolean isManager() {
        return this.role.isManagerRole();
    }

    /**
     * Check if member is a regular member
     */
    public boolean isMember() {
        return this.role.isMemberRole();
    }

    /**
     * Get the role type
     */
    public RoleType getRoleType() {
        return this.role.getRoleType();
    }

    /**
     * Get permissions for this member
     */
    public Set<Permission> getPermissions() {
        return this.role.getPermissions();
    }

    /**
     * Check if member has a specific permission
     */
    public boolean hasPermission(Permission permission) {
        return this.role.hasPermission(permission);
    }
}