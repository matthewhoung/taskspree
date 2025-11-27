package com.hongsolo.taskspree.modules.identity.domain.identity.repository;

import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityUser;

import java.util.Optional;
import java.util.UUID;

public interface IIdentityUserManager {

    IdentityUser save(IdentityUser identityUser);

    Optional<IdentityUser> findById(UUID id);

    Optional<IdentityUser> findByEmail(String email);

    boolean existsByEmail(String email);
}