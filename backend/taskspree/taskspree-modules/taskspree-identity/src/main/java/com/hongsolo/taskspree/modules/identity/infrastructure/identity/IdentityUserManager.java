package com.hongsolo.taskspree.modules.identity.infrastructure.identity;

import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityUser;
import com.hongsolo.taskspree.modules.identity.domain.identity.repository.IIdentityUserManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdentityUserManager
        extends JpaRepository<IdentityUser, UUID>, IIdentityUserManager {

    Optional<IdentityUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
