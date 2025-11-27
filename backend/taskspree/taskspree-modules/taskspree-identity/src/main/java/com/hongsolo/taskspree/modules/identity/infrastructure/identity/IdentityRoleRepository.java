package com.hongsolo.taskspree.modules.identity.infrastructure.identity;

import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityRole;
import com.hongsolo.taskspree.modules.identity.domain.identity.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Simple JPA repository for IdentityRole entity.
 * Used primarily for role seeding and lookup operations.
 */
@Repository
public interface IdentityRoleRepository extends JpaRepository<IdentityRole, UUID> {

    Optional<IdentityRole> findByRole(RoleType roleType);

    boolean existsByRole(RoleType roleType);
}