package com.hongsolo.taskspree.modules.users.domain.users.repository;

import com.hongsolo.taskspree.modules.users.domain.users.AppUser;

import java.util.Optional;
import java.util.UUID;

public interface IAppUserRepository {

    AppUser save(AppUser appUser);

    Optional<AppUser> findById(UUID id);

    Optional<AppUser> findByIdentityId(UUID identityId);

    Optional<AppUser> findByEmail(String email);

    boolean existsByIdentityId(UUID identityId);

    boolean existsByEmail(String email);
}