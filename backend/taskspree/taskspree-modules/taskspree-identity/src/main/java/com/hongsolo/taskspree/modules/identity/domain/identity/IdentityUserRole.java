package com.hongsolo.taskspree.modules.identity.domain.identity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "identity_user_roles", schema = "identity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdentityUserRole {

    @EmbeddedId
    private IdentityUserRoleKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("identityId")
    @JoinColumn(name = "identity_id", nullable = false)
    private IdentityUser identityUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    private IdentityRole role;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    private IdentityUserRole(IdentityUser identityUser, IdentityRole role) {
        this.id = new IdentityUserRoleKey(identityUser.getId(), role.getId());
        this.identityUser = identityUser;
        this.role = role;
        this.assignedAt = Instant.now();
    }

    public static IdentityUserRole create(IdentityUser identityUser, IdentityRole role) {
        if (identityUser.getId() == null || role.getId() == null) {
            throw new IllegalStateException("Both entities must be persisted before creating relationship");
        }
        return new IdentityUserRole(identityUser, role);
    }
}