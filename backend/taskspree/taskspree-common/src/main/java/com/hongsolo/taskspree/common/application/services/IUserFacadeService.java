package com.hongsolo.taskspree.common.application.services;

import java.util.Optional;
import java.util.UUID;

/**
 * Facade pattern for user-related operations.
 * This abstraction allows modules to interact with user data without
 * directly depending on the Users module.
 */
public interface IUserFacadeService {

    // === Queries ===
    Optional<UserDto> findById(UUID userId);
    Optional<UserDto> findByIdentityId(UUID identityId);
    Optional<UserDto> findByEmail(String email);
    Optional<UserDto> getCurrentUser();

    // === Commands ===
    UUID createUser(CreateUserCommand command);

    // === DTOs ===
    record UserDto(
            UUID userId,
            UUID identityId,
            String email,
            String username
    ) { }

    record CreateUserCommand(
            UUID identityId,
            String email
    ) { }
}
