package com.hongsolo.taskspree.modules.identity.domain.identity.repository;

import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityUserRole;
import com.hongsolo.taskspree.modules.identity.domain.identity.enums.RoleType;

import java.util.List;
import java.util.UUID;

public interface IIdentityRoleManager {

    IdentityUserRole save(IdentityUserRole identityUserRole);

    List<IdentityUserRole> findByIdentityId(UUID identityId);

    boolean existsByIdentityIdAndRoleType(UUID identityId, RoleType roleType);
}