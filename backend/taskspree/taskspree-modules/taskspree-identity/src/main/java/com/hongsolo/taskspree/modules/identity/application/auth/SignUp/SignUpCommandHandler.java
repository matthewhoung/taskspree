package com.hongsolo.taskspree.modules.identity.application.auth.SignUp;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityErrors;
import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityRole;
import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityUser;
import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityUserRole;
import com.hongsolo.taskspree.modules.identity.domain.identity.enums.RoleType;
import com.hongsolo.taskspree.modules.identity.domain.identity.repository.IIdentityRoleManager;
import com.hongsolo.taskspree.modules.identity.domain.identity.repository.IIdentityUserManager;
import com.hongsolo.taskspree.modules.identity.infrastructure.identity.IdentityRoleRepository;
import com.hongsolo.taskspree.modules.users.domain.users.AppUser;
import com.hongsolo.taskspree.modules.users.domain.users.repository.IAppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignUpCommandHandler implements CommandHandler<SignUpCommand, Result<SignUpResponse>> {

    private final IIdentityUserManager identityUserManager;
    private final IIdentityRoleManager identityRoleManager;
    private final IdentityRoleRepository identityRoleRepository;
    private final IAppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Result<SignUpResponse> handle(SignUpCommand command) {
        log.info("Precessing sign-up for email: {}", command.email());

        // Check if email already exists
        if (identityUserManager.existsByEmail((command.email()))) {
            log.warn("Sign up failed: Email {} is already in use", command.email());

            return Result.failure(IdentityErrors.EMAIL_ALREADY_EXISTS);
        }

        // Hash password and create identity user
        String passwordHash = passwordEncoder.encode(command.password());
        IdentityUser identityUser = IdentityUser.create(command.email(), passwordHash);
        identityUser = identityUserManager.save(identityUser);

        log.debug("Created identity user with ID: {}", identityUser.getId());

        // get user role and assign to user
        IdentityRole userRole = identityRoleRepository.findByRole(RoleType.USER)
                .orElseThrow(() -> new IllegalStateException("role not found: Ensure RoleSeeder ran before user sign up"));

        IdentityUserRole identityUserRole = IdentityUserRole.create(identityUser, userRole);
        identityRoleManager.save(identityUserRole);

        log.debug("Assigned USER role to identity: {}", identityUser.getId());

        // Create AppUser in users module
        AppUser appUser = AppUser.create(identityUser.getId(), command.email());
        appUserRepository.save(appUser);

        log.debug("Created app user with ID: {}", appUser.getId());

        log.info("Signup successful for email: {}", command.email());

        return Result.success(SignUpResponse.of(identityUser.getId(), command.email()));
    }
}
