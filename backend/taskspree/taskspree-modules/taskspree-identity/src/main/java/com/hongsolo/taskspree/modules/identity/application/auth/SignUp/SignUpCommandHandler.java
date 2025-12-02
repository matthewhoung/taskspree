package com.hongsolo.taskspree.modules.identity.application.auth.SignUp;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.application.services.IMarketplaceFacadeService;
import com.hongsolo.taskspree.common.application.services.IUserFacadeService;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityErrors;
import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityRole;
import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityUser;
import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityUserRole;
import com.hongsolo.taskspree.modules.identity.domain.identity.enums.RoleType;
import com.hongsolo.taskspree.modules.identity.domain.identity.repository.IIdentityRoleManager;
import com.hongsolo.taskspree.modules.identity.domain.identity.repository.IIdentityUserManager;
import com.hongsolo.taskspree.modules.identity.infrastructure.identity.IdentityRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignUpCommandHandler implements CommandHandler<SignUpCommand, Result<SignUpResponse>> {

    private final IIdentityUserManager identityUserManager;
    private final IIdentityRoleManager identityRoleManager;
    private final IdentityRoleRepository identityRoleRepository;
    private final IUserFacadeService userFacadeService;
    private final IMarketplaceFacadeService marketplaceFacadeService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Result<SignUpResponse> handle(SignUpCommand command) {
        log.info("Processing sign-up for email: {}", command.email());

        // 1. Check if email already exists
        if (identityUserManager.existsByEmail((command.email()))) {
            log.warn("Sign up failed: Email {} is already in use", command.email());
            return Result.failure(IdentityErrors.EMAIL_ALREADY_EXISTS);
        }

        // 2. Hash password and create identity user
        String passwordHash = passwordEncoder.encode(command.password());
        IdentityUser identityUser = IdentityUser.create(command.email(), passwordHash);
        identityUser = identityUserManager.save(identityUser);

        log.debug("Created identity user with ID: {}", identityUser.getId());

        // 3. Get user role and assign to user
        IdentityRole userRole = identityRoleRepository.findByRole(RoleType.USER)
                .orElseThrow(() -> new IllegalStateException("Role not found: Ensure RoleSeeder ran before user sign up"));

        IdentityUserRole identityUserRole = IdentityUserRole.create(identityUser, userRole);
        identityRoleManager.save(identityUserRole);

        log.debug("Assigned USER role to identity: {}", identityUser.getId());

        // 4. Create AppUser in users module
        UUID appUserId = userFacadeService.createUser(
                new IUserFacadeService.CreateUserCommand(
                        identityUser.getId(),
                        command.email()
                ));

        log.debug("Created app user with ID: {}", appUserId);

        // 5. Create default marketplace for the new user (NEW)
        String username = command.email().split("@")[0];
        String marketplaceName = username + "'s Marketplace";

        try {
            UUID marketplaceId = marketplaceFacadeService.createDefaultMarketplace(
                    new IMarketplaceFacadeService.CreateDefaultMarketplaceCommand(
                            appUserId,
                            marketplaceName
                    )
            );
            log.debug("Created default marketplace with ID: {}", marketplaceId);
        } catch (Exception e) {
            // Log but don't fail signup if marketplace creation fails
            // User can create marketplace manually later
            log.warn("Failed to create default marketplace for user {}: {}", appUserId, e.getMessage());
        }

        log.info("Signup successful for email: {}", command.email());

        return Result.success(SignUpResponse.of(identityUser.getId(), command.email()));
    }
}
