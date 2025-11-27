package com.hongsolo.taskspree.modules.identity.infrastructure.seed;

import com.hongsolo.taskspree.modules.identity.domain.identity.IdentityRole;
import com.hongsolo.taskspree.modules.identity.domain.identity.enums.RoleType;
import com.hongsolo.taskspree.modules.identity.infrastructure.identity.IdentityRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RoleSeeder implements ApplicationRunner {

    private final IdentityRoleRepository roleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting role seeding...");

        for (RoleType roleType : RoleType.values()) {
            seedRoleIfNotExists(roleType);
        }

        log.info("Role seeding completed.");
    }

    private void seedRoleIfNotExists(RoleType roleType) {
        if (!roleRepository.existsByRole(roleType)) {
            IdentityRole role = IdentityRole.create(roleType);
            roleRepository.save(role);
            log.info("Seeded role: {} - {}", roleType.name(), roleType.getDescription());
        } else {
            log.debug("Role already exists: {}", roleType.name());
        }
    }
}