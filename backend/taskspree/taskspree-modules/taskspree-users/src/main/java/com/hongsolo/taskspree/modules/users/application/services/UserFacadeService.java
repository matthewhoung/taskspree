package com.hongsolo.taskspree.modules.users.application.services;

import com.hongsolo.taskspree.common.application.services.IUserFacadeService;
import com.hongsolo.taskspree.modules.users.domain.users.AppUser;
import com.hongsolo.taskspree.modules.users.domain.users.repository.IAppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFacadeService implements IUserFacadeService {

    private final IAppUserRepository appUserRepository;

    @Override
    public Optional<UserDto> findById(UUID userId) {
        return appUserRepository.findById(userId)
                .map(this::toUserDto);
    }

    @Override
    public Optional<UserDto> findByIdentityId(UUID identityId) {
        return appUserRepository.findByIdentityId(identityId)
                .map(this::toUserDto);
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
        return appUserRepository.findByEmail(email)
                .map(this::toUserDto);
    }

    @Override
    public Optional<UserDto> getCurrentUser() {
        return getIdentityIdFromContext()
                .flatMap(this::findByIdentityId);
    }

    @Override
    @Transactional
    public UUID createUser(CreateUserCommand command) {
        if (appUserRepository.existsByIdentityId(command.identityId())) {
            throw new IllegalStateException("User with identity ID already exists: " + command.identityId());
        }

        AppUser appUser = AppUser.create(
                command.identityId(),
                command.email()
        );

        appUser = appUserRepository.save(appUser);

        return appUser.getId();
    }

    // === Helpers ===
    private UserDto toUserDto(AppUser user) {
        return new UserDto(
                user.getId(),
                user.getIdentityId(),
                user.getEmail(),
                user.getUsername()
        );
    }

    private Optional<UUID> getIdentityIdFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UUID) {
            return Optional.of((UUID) principal);
        }

        return Optional.empty();
    }
}
