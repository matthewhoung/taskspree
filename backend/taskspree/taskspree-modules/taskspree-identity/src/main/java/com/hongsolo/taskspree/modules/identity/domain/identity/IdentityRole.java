package com.hongsolo.taskspree.modules.identity.domain.identity;

import com.hongsolo.taskspree.common.domain.BaseEntity;
import com.hongsolo.taskspree.modules.identity.domain.identity.enums.RoleType;
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
    @Column(name = "role", nullable = false, unique = true)
    private RoleType role;

    @Column(name = "description", length = 255)
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