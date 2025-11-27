package com.hongsolo.taskspree.modules.identity.domain.identity;

import com.hongsolo.taskspree.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "identity_users", schema = "identity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdentityUser extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "disabled_at")
    private Instant disabledAt;

    @Column(name = "disabled_reason", length = 255)
    private String disabledReason;

    @OneToMany(mappedBy = "identityUser", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<IdentityUserRole> roles = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    private IdentityUser(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static IdentityUser create(String email, String passwordHash) {
        return new IdentityUser(email, passwordHash);
    }

    public void addRole(IdentityRole role) {
        IdentityUserRole userRole = IdentityUserRole.create(this, role);
        roles.add(userRole);
    }

    public void updatePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.updatedAt = Instant.now();
    }

    public void disableAccount(String reason) {
        this.enabled = false;
        this.disabledAt = Instant.now();
        this.disabledReason = reason;
        this.updatedAt = Instant.now();
    }

    public void enableAccount() {
        this.enabled = true;
        this.disabledAt = null;
        this.disabledReason = null;
        this.updatedAt = Instant.now();
    }
}