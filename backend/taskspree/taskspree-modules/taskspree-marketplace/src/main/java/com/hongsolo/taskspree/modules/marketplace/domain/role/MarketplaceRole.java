package com.hongsolo.taskspree.modules.marketplace.domain.role;

import com.hongsolo.taskspree.common.domain.BaseEntity;
import com.hongsolo.taskspree.modules.marketplace.domain.marketplace.Marketplace;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.Permission;
import com.hongsolo.taskspree.modules.marketplace.domain.role.enums.RoleType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "marketplace_roles", schema = "marketplace")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketplaceRole extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marketplace_id", nullable = false)
    private Marketplace marketplace;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 20)
    private RoleType roleType;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private MarketplaceRole(Marketplace marketplace, RoleType roleType, String displayName, String description) {
        this.marketplace = marketplace;
        this.roleType = roleType;
        this.displayName = displayName;
        this.description = description;
        this.createdAt = Instant.now();
    }

    /**
     * Factory method to create a role with default display name
     */
    public static MarketplaceRole create(Marketplace marketplace, RoleType roleType) {
        return new MarketplaceRole(
                marketplace,
                roleType,
                roleType.getDefaultDisplayName(),
                roleType.getDefaultDescription()
        );
    }

    /**
     * Factory method to create a role with custom display name
     */
    public static MarketplaceRole create(Marketplace marketplace, RoleType roleType, String displayName, String description) {
        return new MarketplaceRole(marketplace, roleType, displayName, description);
    }

    /**
     * Update display name and description
     */
    public void updateDisplayInfo(String displayName, String description) {
        if (displayName != null && !displayName.isBlank()) {
            this.displayName = displayName;
        }
        if (description != null) {
            this.description = description;
        }
    }

    /**
     * Get permissions for this role (delegated to RoleType)
     */
    public Set<Permission> getPermissions() {
        return roleType.getPermissions();
    }

    /**
     * Check if role has a specific permission
     */
    public boolean hasPermission(Permission permission) {
        return roleType.hasPermission(permission);
    }

    /**
     * Check if this is the owner role
     */
    public boolean isOwnerRole() {
        return roleType == RoleType.OWNER;
    }

    /**
     * Check if this is the manager role
     */
    public boolean isManagerRole() {
        return roleType == RoleType.MANAGER;
    }

    /**
     * Check if this is the member role
     */
    public boolean isMemberRole() {
        return roleType == RoleType.MEMBER;
    }
}