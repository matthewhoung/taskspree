package com.hongsolo.taskspree.modules.identity.domain;

import com.hongsolo.taskspree.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "identity_roles", schema = "identity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdentityRole extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleType role;

    @Column(length = 255)
    private String description;

    private IdentityRole(RoleType role, String description) {
        this.role = role;
        this.description = description;
    }

    public static IdentityRole create(RoleType role) {
        return new IdentityRole(role, role.getDescription());
    }

    public static IdentityRole create(RoleType role, String customDescription) {
        return new IdentityRole(role, customDescription);
    }
}