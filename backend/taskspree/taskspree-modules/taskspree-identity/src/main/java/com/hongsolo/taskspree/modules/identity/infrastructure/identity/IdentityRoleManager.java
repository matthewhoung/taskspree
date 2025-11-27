package com.hongsolo.taskspree.modules.identity.infrastructure.identity;

import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityUserRole;
import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityUserRoleKey;
import com.hongsolo.taskspree.modules.identity.domain.identity.enums.RoleType;
import com.hongsolo.taskspree.modules.identity.domain.identity.repository.IIdentityRoleManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IdentityRoleManager
        extends JpaRepository<IdentityUserRole, IdentityUserRoleKey>, IIdentityRoleManager {

    /**
     * JPA method naming - hop through identityUser -> id field.
     */
    List<IdentityUserRole> findByIdentityUser_Id(UUID identityId);

    /**
     * JPA method naming - hop through identityUser -> id and role -> role fields.
     */
    boolean existsByIdentityUser_IdAndRole_Role(UUID identityId, RoleType roleType);

    // ==================== Default Method Bridges ====================

    @Override
    default List<IdentityUserRole> findByIdentityId(UUID identityId) {
        return findByIdentityUser_Id(identityId);
    }

    @Override
    default boolean existsByIdentityIdAndRoleType(UUID identityId, RoleType roleType) {
        return existsByIdentityUser_IdAndRole_Role(identityId, roleType);
    }
}