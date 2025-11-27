package com.hongsolo.taskspree.modules.identity.domain.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class IdentityUserRoleKey implements Serializable {

    @Column(name = "identity_id")
    private UUID identityId;

    @Column(name = "role_id")
    private UUID roleId;
}