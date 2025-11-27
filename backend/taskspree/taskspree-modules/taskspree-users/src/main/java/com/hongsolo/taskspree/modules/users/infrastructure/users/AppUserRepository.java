package com.hongsolo.taskspree.modules.users.infrastructure.users;

import com.hongsolo.taskspree.modules.users.domain.users.AppUser;
import com.hongsolo.taskspree.modules.users.domain.users.repository.IAppUserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository
        extends JpaRepository<AppUser, UUID>, IAppUserRepository {

    Optional<AppUser> findByIdentityId(UUID identityId);

    Optional<AppUser> findByEmail(String email);

    boolean existsByIdentityId(UUID identityId);

    boolean existsByEmail(String email);
}